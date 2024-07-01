package io.spbx.webby.app;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import io.spbx.util.base.CharArray;
import io.spbx.util.base.EasyExceptions.IllegalArgumentExceptions;
import io.spbx.util.base.EasyExceptions.InternalErrors;
import io.spbx.util.base.EasyNulls;
import io.spbx.util.base.MutableCharArray;
import io.spbx.util.base.Unchecked;
import io.spbx.util.collect.EasyMaps;
import io.spbx.util.collect.ListBuilder;
import io.spbx.util.func.Reversible;
import io.spbx.util.io.EasyIo;
import jodd.util.Wildcard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static io.spbx.util.base.EasyExceptions.newInternalError;
import static java.util.Objects.requireNonNull;

@Beta
final class ClassFilterStringConverter implements Reversible<ClassFilter, String> {
    public static final Reversible<ClassFilter, String> TO_STRING = new ClassFilterStringConverter();
    public static final Reversible<String, ClassFilter> TO_CLASS_FILTER = TO_STRING.reverse();

    private static final Charset CHARSET = StandardCharsets.US_ASCII;
    private static final String BASE64_PREFIX = "base64:";
    private static final ImmutableMap<CharArray, ClassFilter> KNOWN_FILTERS = ImmutableMap.of(
        new CharArray("all"), ClassFilter.ALL,
        new CharArray("none"), ClassFilter.NONE,
        new CharArray("default"), ClassFilter.DEFAULT
    );
    private static final ImmutableMap<ClassFilter, CharArray> INVERSE_FILTERS = EasyMaps.inverseMap(KNOWN_FILTERS);

    @Override
    public @NotNull String forward(@NotNull ClassFilter filter) {
        return EasyNulls.<String>firstNonNull(
            () -> {
                CharArray known = INVERSE_FILTERS.get(filter);
                return known != null ? known.toString() : null;
            },
            () -> {
                // https://stackoverflow.com/questions/22807912/how-to-serialize-a-lambda
                byte[] serialized = EasyIo.serialize(filter.predicate());
                return BASE64_CONVERTER.forward(serialized);
            }
        );
    }

    @Override
    public @NotNull ClassFilter backward(@NotNull String input) {
        CharArray array = new CharArray(input);
        return EasyNulls.<ClassFilter>firstNonNull(
            () -> {
                if (array.startsWith(BASE64_PREFIX)) {
                    byte[] decoded = BASE64_CONVERTER.backward(input);
                    return ClassFilter.of(EasyIo.deserialize(decoded));
                }
                return null;
            },
            () -> {
                PropertyParser parser = new PropertyParser(input);
                return parser.parse();
            }
        );
    }

    @VisibleForTesting
    static final Reversible<byte[], String> BASE64_CONVERTER = new Reversible<>() {
        @Override
        public @NotNull String forward(byte @NotNull [] bytes) {
            return encodeBase64(bytes, BASE64_PREFIX, CHARSET);
        }

        @Override
        public byte @NotNull [] backward(@NotNull String str) {
            return decodeBase64(str, BASE64_PREFIX, CHARSET);
        }
    };

    @VisibleForTesting
    static @NotNull String encodeBase64(byte @NotNull [] bytes, @NotNull String prefix, @NotNull Charset charset) {
        byte[] prefixBytes = prefix.getBytes(charset);
        int size = prefixBytes.length + EasyIo.base64LengthNoPadding(bytes.length);
        try (ByteArrayOutputStream output = new ByteArrayOutputStream(size)) {
            output.write(prefixBytes);
            try (OutputStream wrap = Base64.getUrlEncoder().withoutPadding().wrap(output)) {
                wrap.write(bytes);
            }
            String result = output.toString(charset);
            assert result.length() == size : newInternalError("Wrong size allocated: result=%s, size=%s", result, size);
            return result;
        } catch (IOException e) {
            return Unchecked.rethrow(e);
        }
    }

    @VisibleForTesting
    static byte @NotNull [] decodeBase64(@NotNull String str, @NotNull String prefix, @NotNull Charset charset) {
        assert str.startsWith(prefix);
        byte[] bytes = str.getBytes(charset);
        ByteBuffer buffer = ByteBuffer.wrap(bytes, prefix.length(), bytes.length - prefix.length());
        return Base64.getUrlDecoder().decode(buffer).array();
    }

    @VisibleForTesting
    static class PropertyParser {
        private final CharArray input;

        PropertyParser(@NotNull CharArray input) {
            this.input = input;
        }

        PropertyParser(@NotNull String input) {
            this(new CharArray(input));
        }

        @Nullable ClassFilter parse() {
            return input.isEmpty() ? null : parseExpression();
        }

        @NotNull ClassFilter parseExpression() {
            int start = -1;
            int balance = 0;
            int total = 0;
            boolean neg = false;
            InfixOp infixOp = null;
            ClassFilter current = null;
            for (int i = 0; i < input.length(); i++) {
                int ch = input.at(i);
                if (ch == '(' || ch == ')') {
                    total++;
                    balance += ch == '(' ? 1 : -1;
                    IllegalArgumentExceptions.failIf(balance < 0, "Invalid expression: %s", input);
                    if (balance == 0 && ch == ')') {
                        assert input.at(start) == '(' && input.at(i) == ')';

                        PropertyParser parser = new PropertyParser(input.substring(start + 1, i));
                        ClassFilter filter = total > 2 ? parser.parseExpression() : parser.parseTerm();
                        if (neg) {
                            filter = ClassFilter.of(filter.predicate().negate());
                        }

                        current = infixOp != null ?
                            infixOp.func.apply(requireNonNull(current), filter) :
                            filter;

                        infixOp = switch (input.at(i + 1)) {
                            case '&' -> InfixOp.AND;
                            case '|' -> InfixOp.OR;
                            case '^' -> InfixOp.XOR;
                            case -1 -> null;
                            default -> IllegalArgumentExceptions.fail("Unexpected value: %s", input.at(i + 1));
                        };
                    } else if (balance == 1 && ch == '(') {
                        start = i;
                        neg = input.at(i - 1) == '!';
                    }
                }
            }
            if (total == 0) {
                return parseTerm();
            }
            return requireNonNull(current, () -> "Failed to parse empty input: `%s`".formatted(input));
        }

        enum InfixOp {
            AND(ClassFilter::matchingAllOf),
            OR(ClassFilter::matchingAnyOf),
            XOR((first, second) -> ClassFilter.of(first.predicate().xor(second.predicate())));

            private final BiFunction<ClassFilter, ClassFilter, ClassFilter> func;

            InfixOp(BiFunction<ClassFilter, ClassFilter, ClassFilter> func) {
                this.func = func;
            }
        }

        @NotNull ClassFilter parseTerm() {
            ClassFilter known = KNOWN_FILTERS.get(input);
            if (known != null) {
                return known;
            }

            Collector<RuleSet, ?, Map<Cmp, RuleSet>> toMap = Collectors.toMap(RuleSet::cmp, Function.identity());
            Map<Cmp, RuleSet> pkgRules = Arrays.stream(Cmp.values()).map(cmp -> RuleSet.of(Kind.PACKAGE, cmp)).collect(toMap);
            Map<Cmp, RuleSet> clsRules = Arrays.stream(Cmp.values()).map(cmp -> RuleSet.of(Kind.CLASS, cmp)).collect(toMap);

            input.split(',', array ->
                parsePredicate(array, Map.of(Kind.CLASS, clsRules, Kind.PACKAGE, pkgRules))
            );

            Predicate<String> matchPkg = buildMatcher(pkgRules.values());
            Predicate<String> matchCls = buildMatcher(clsRules.values());
            return ClassFilter.of((pkg, cls) -> matchPkg.test(pkg) && matchCls.test(cls));
        }

        private static @NotNull Predicate<String> buildMatcher(@NotNull Collection<RuleSet> rules) {
            Predicate<String>[] predicates = ListBuilder.of(rules)
                .map(ruleSet -> buildPredicate(ruleSet.rules, ruleSet.cmp.matchSingle, ruleSet.cmp.matchBatch))
                .withoutNulls()
                .toNativeArray(Predicate[]::new);

            if (predicates.length == 1) {
                return predicates[0];
            }
            if (predicates.length == 2) {
                return predicates[0].or(predicates[1]);
            }
            if (predicates.length == 3) {
                return predicates[0].or(predicates[1]).or(predicates[2]);
            }
            return Arrays.stream(predicates).reduce(Predicate::or).orElse(str -> true);
        }

        private static @Nullable Predicate<String> buildPredicate(@NotNull List<String> list,
                                                                  @NotNull BiPredicate<String, String> single,
                                                                  @NotNull BiPredicate<List<String>, String> batch) {
            if (list.isEmpty()) {
                return null;
            }
            String first = list.getFirst();
            String last = list.getLast();
            if (list.size() == 1) {
                return str -> single.test(str, first);
            }
            if (list.size() == 2) {
                return str -> single.test(str, first) || single.test(str, last);
            }
            return str -> batch.test(list, str);
        }

        static void parsePredicate(@NotNull CharArray input, @NotNull Map<Kind, Map<Cmp, RuleSet>> rules) {
            ParsedPredicate parsed = ParsedPredicate.parseFrom(input);
            RuleSet ruleSet =
                InternalErrors.assureNonNull(rules.get(parsed.kind).get(parsed.cmp), "Missing rule set: %s", parsed);
            assert ruleSet.matches(parsed);
            ruleSet.accept(parsed.value());
        }

        record ParsedPredicate(@NotNull Kind kind, @NotNull Cmp cmp, @NotNull CharArray value) {
            static @NotNull ParsedPredicate parseFrom(@NotNull CharArray input) {
                int i = input.indexOf('=');
                IllegalArgumentExceptions.assure(i > 0, "Invalid predicate: %s", input);

                CharArray kind_ = input.substring(0, i);
                MutableCharArray value_ = input.substringFrom(i + 1).mutableCopy();

                Kind kind = Kind.parseFrom(kind_);
                Cmp cmp = Cmp.parseAndTrimValue(value_);
                CharArray value = value_.immutable();
                return new ParsedPredicate(kind, cmp, value);
            }
        }

        record RuleSet(@NotNull Kind kind, @NotNull Cmp cmp, @NotNull List<String> rules) implements Consumer<CharArray> {
            static @NotNull RuleSet of(@NotNull Kind kind, @NotNull Cmp cmp) {
                return new RuleSet(kind, cmp, new ArrayList<>());
            }

            boolean matches(@NotNull ParsedPredicate predicate) {
                return kind == predicate.kind && cmp == predicate.cmp;
            }

            @Override
            public void accept(@NotNull CharArray array) {
                rules.add(array.toString());
            }
        }

        enum Kind {
            PACKAGE,
            CLASS;

            static @NotNull Kind parseFrom(@NotNull CharArray array) {
                return array.contentEquals("pkg") ? PACKAGE :
                       array.contentEquals("cls") ? CLASS :
                       IllegalArgumentExceptions.fail("Invalid kind: %s", array);
            }
        }

        enum Cmp {
            EQUALS(String::equals, List::contains),
            STARTS_WITH(String::startsWith, (list, str) -> list.stream().anyMatch(str::startsWith)),
            ENDS_WITH(String::endsWith, (list, str) -> list.stream().anyMatch(str::endsWith)),
            CONTAINS(String::contains, (list, str) -> list.stream().anyMatch(str::contains)),
            WILDCARD_MATCH(Wildcard::match, (list, str) -> list.stream().anyMatch(pattern -> Wildcard.match(str, pattern)));

            private final BiPredicate<String, String> matchSingle;
            private final BiPredicate<List<String>, String> matchBatch;

            Cmp(@NotNull BiPredicate<String, String> matchSingle, @NotNull BiPredicate<List<String>, String> matchBatch) {
                this.matchSingle = matchSingle;
                this.matchBatch = matchBatch;
            }

            static @NotNull Cmp parseAndTrimValue(@NotNull MutableCharArray val) {
                boolean starts = val.startsWith('*');
                boolean ends = val.endsWith('*');
                boolean inside = val.length() > 2 && val.substring(1, -1).contains('*');
                boolean question = val.contains('?');
                boolean wildcard = inside || question;
                if (wildcard) {
                    return WILDCARD_MATCH;
                } else if (starts && ends) {
                    val.offsetPrefix('*');
                    val.offsetSuffix('*');
                    return CONTAINS;
                } else if (starts) {
                    val.offsetStart(1);
                    return ENDS_WITH;
                } else if (ends) {
                    val.offsetEnd(1);
                    return STARTS_WITH;
                } else {
                    return EQUALS;
                }
            }
        }
    }
}
