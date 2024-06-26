package io.spbx.webby.app;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import io.spbx.util.base.CharArray;
import io.spbx.util.base.EasyExceptions.IllegalArgumentExceptions;
import io.spbx.util.base.EasyNulls;
import io.spbx.util.base.MutableCharArray;
import io.spbx.util.base.Unchecked;
import io.spbx.util.collect.EasyMaps;
import io.spbx.util.collect.ListBuilder;
import io.spbx.util.func.Reversible;
import io.spbx.util.io.EasyIo;
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
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static io.spbx.webby.app.ClassFilterStringConverter.PropertyParser.ParsedPredicate.parseFrom;
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
            assert result.length() == size : "Internal error: wrong size allocated";
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

        PropertyParser(@NotNull String input, int start, int len) {
            this.input = new CharArray(input, start, start + len);
        }

        PropertyParser(@NotNull String input) {
            this(input, 0, input.length());
        }

        @NotNull ClassFilter parse() {
            return parseTerm(input);
        }

        static @NotNull ClassFilter parseTerm(@NotNull CharArray input) {
            ClassFilter known = KNOWN_FILTERS.get(input);
            if (known != null) {
                return known;
            }

            Collector<RuleSet, ?, Map<Op, RuleSet>> toMap = Collectors.toMap(RuleSet::op, Function.identity());
            Map<Op, RuleSet> pkgRules = Arrays.stream(Op.values()).map(op -> RuleSet.of(Kind.PACKAGE, op)).collect(toMap);
            Map<Op, RuleSet> clsRules = Arrays.stream(Op.values()).map(op -> RuleSet.of(Kind.CLASS, op)).collect(toMap);

            input.split(',', array ->
                parsePredicate(array, Map.of(Kind.CLASS, clsRules, Kind.PACKAGE, pkgRules))
            );

            Predicate<String> matchPkg = buildMatcher(pkgRules.values());
            Predicate<String> matchCls = buildMatcher(clsRules.values());
            return ClassFilter.of((pkg, cls) -> matchPkg.test(pkg) && matchCls.test(cls));
        }

        private static @NotNull Predicate<String> buildMatcher(@NotNull Collection<RuleSet> rules) {
            Predicate<String>[] predicates = ListBuilder.of(rules)
                .map(ruleSet -> buildPredicate(ruleSet.rules, ruleSet.op.matchSingle, ruleSet.op.matchBatch))
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

        static void parsePredicate(@NotNull CharArray input, @NotNull Map<Kind, Map<Op, RuleSet>> rules) {
            ParsedPredicate parsed = parseFrom(input);
            RuleSet ruleSet = requireNonNull(rules.get(parsed.kind()).get(parsed.op()),
                                             () -> "Internal error: Missing rule set for: " + parsed);
            assert ruleSet.matches(parsed);
            ruleSet.accept(parsed.value());
        }

        record ParsedPredicate(@NotNull Kind kind, @NotNull Op op, @NotNull CharArray value) {
            static @NotNull ParsedPredicate parseFrom(@NotNull CharArray input) {
                int j = input.indexOf('=');
                assert j > 0 : "Invalid predicate: " + input;
                int i = input.at(j - 1) == '~' ? j - 1 : j;

                CharArray kind_ = input.substring(0, i);
                CharArray op_ = input.substring(i, j + 1);
                MutableCharArray value_ = input.substringFrom(j + 1).mutableCopy();

                Kind kind = Kind.parseFrom(kind_);
                Op op = Op.parseAndTrimValue(op_, value_);
                CharArray value = value_.immutable();
                return new ParsedPredicate(kind, op, value);
            }
        }

        record RuleSet(@NotNull Kind kind, @NotNull Op op, @NotNull List<String> rules) implements Consumer<CharArray> {
            static @NotNull RuleSet of(@NotNull Kind kind, @NotNull Op op) {
                return new RuleSet(kind, op, new ArrayList<>());
            }

            boolean matches(@NotNull ParsedPredicate predicate) {
                return kind == predicate.kind && op == predicate.op;
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

        enum Op {
            EQUALS(String::equals, List::contains),
            STARTS_WITH(String::startsWith, (list, str) -> list.stream().anyMatch(str::startsWith)),
            ENDS_WITH(String::endsWith, (list, str) -> list.stream().anyMatch(str::endsWith)),
            CONTAINS(String::contains, (list, str) -> list.stream().anyMatch(str::contains));

            private final BiPredicate<String, String> matchSingle;
            private final BiPredicate<List<String>, String> matchBatch;

            Op(@NotNull BiPredicate<String, String> matchSingle, @NotNull BiPredicate<List<String>, String> matchBatch) {
                this.matchSingle = matchSingle;
                this.matchBatch = matchBatch;
            }

            static @NotNull Op parseAndTrimValue(@NotNull CharArray op, @NotNull MutableCharArray val) {
                if (op.contentEquals('=')) {
                    return EQUALS;
                }

                assert op.contentEquals("~=") : "Invalid operation: " + op;
                boolean starts = val.startsWith('*');
                boolean ends = val.endsWith('*');
                if (starts && ends) {
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
                    throw IllegalArgumentExceptions.form("Invalid contains operation for the value: %s", val);
                }
            }
        }
    }
}
