/** Top level JS functions are defined in index.html script tag */
external fun topLevelJsFun(): dynamic

fun jsTypeOf(o: Any) = js("typeof o") as String
