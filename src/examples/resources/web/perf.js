// https://stackoverflow.com/questions/9637517/parsing-relaxed-json-without-eval
function parseRelaxedJson(input) {
    let sanitized = input.replace(/(['"])?([a-z0-9A-Z_]+)(['"])?:/g, '"$2": ');
    return JSON.parse(sanitized);
}

if (window.performance && performance.getEntriesByType) { // avoid error in Safari 10, IE9- and other old browsers
    let navTiming = performance.getEntriesByType('navigation')
    console.log(navTiming);
    if (navTiming.length > 0) { // still not supported as of Safari 14...
        let serverTiming = navTiming[0].serverTiming
        if (serverTiming && serverTiming.length > 0) {
            for (let i=0; i<serverTiming.length; i++) {
                console.log(`${serverTiming[i].name} = ${serverTiming[i].description}`)
            }
        }
    }
}
