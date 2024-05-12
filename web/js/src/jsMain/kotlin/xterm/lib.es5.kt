package xterm

typealias Pick<T, K> = Any

// inline operator fun <T, U> IEvent<T, U>.invoke(noinline listener: (arg1: T, arg2: U) -> Any) :
// IDisposable{
//    return asDynamic()(listener).unsafeCast<IDisposable>()
// }
