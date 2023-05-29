@file:JsModule("xterm")
@file:JsNonModule
@file:Suppress(
    "INTERFACE_WITH_SUPERCLASS",
    "OVERRIDING_FINAL_MEMBER",
    "RETURN_TYPE_MISMATCH_ON_OVERRIDE",
    "CONFLICTING_OVERLOADS")

package xterm

import kotlin.js.RegExp
import org.khronos.webgl.*
import org.w3c.dom.*
import org.w3c.dom.events.*

external interface ITerminalOptions {
  var allowProposedApi: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var allowTransparency: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var altClickMovesCursor: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var bellSound: String?
    get() = definedExternally
    set(value) = definedExternally

  var bellStyle: String? /* "none" | "sound" */
    get() = definedExternally
    set(value) = definedExternally

  var convertEol: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var cols: Number?
    get() = definedExternally
    set(value) = definedExternally

  var cursorBlink: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var cursorStyle: String? /* "block" | "underline" | "bar" */
    get() = definedExternally
    set(value) = definedExternally

  var cursorWidth: Number?
    get() = definedExternally
    set(value) = definedExternally

  var disableStdin: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var drawBoldTextInBrightColors: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var fastScrollModifier: String? /* "alt" | "ctrl" | "shift" */
    get() = definedExternally
    set(value) = definedExternally

  var fastScrollSensitivity: Number?
    get() = definedExternally
    set(value) = definedExternally

  var fontSize: Number?
    get() = definedExternally
    set(value) = definedExternally

  var fontFamily: String?
    get() = definedExternally
    set(value) = definedExternally

  var fontWeight:
      dynamic /* "normal" | "bold" | "100" | "200" | "300" | "400" | "500" | "600" | "700" | "800" | "900" | Number? */
    get() = definedExternally
    set(value) = definedExternally

  var fontWeightBold:
      dynamic /* "normal" | "bold" | "100" | "200" | "300" | "400" | "500" | "600" | "700" | "800" | "900" | Number? */
    get() = definedExternally
    set(value) = definedExternally

  var letterSpacing: Number?
    get() = definedExternally
    set(value) = definedExternally

  var lineHeight: Number?
    get() = definedExternally
    set(value) = definedExternally

  var linkTooltipHoverDuration: Number?
    get() = definedExternally
    set(value) = definedExternally

  var logLevel: String? /* "debug" | "info" | "warn" | "error" | "off" */
    get() = definedExternally
    set(value) = definedExternally

  var macOptionIsMeta: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var macOptionClickForcesSelection: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var minimumContrastRatio: Number?
    get() = definedExternally
    set(value) = definedExternally

  var rendererType: String? /* "dom" | "canvas" */
    get() = definedExternally
    set(value) = definedExternally

  var rightClickSelectsWord: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var rows: Number?
    get() = definedExternally
    set(value) = definedExternally

  var screenReaderMode: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var scrollback: Number?
    get() = definedExternally
    set(value) = definedExternally

  var scrollSensitivity: Number?
    get() = definedExternally
    set(value) = definedExternally

  var tabStopWidth: Number?
    get() = definedExternally
    set(value) = definedExternally

  var theme: ITheme?
    get() = definedExternally
    set(value) = definedExternally

  var windowsMode: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var wordSeparator: String?
    get() = definedExternally
    set(value) = definedExternally

  var windowOptions: IWindowOptions?
    get() = definedExternally
    set(value) = definedExternally
}

external interface ITheme {
  var foreground: String?
    get() = definedExternally
    set(value) = definedExternally

  var background: String?
    get() = definedExternally
    set(value) = definedExternally

  var cursor: String?
    get() = definedExternally
    set(value) = definedExternally

  var cursorAccent: String?
    get() = definedExternally
    set(value) = definedExternally

  var selection: String?
    get() = definedExternally
    set(value) = definedExternally

  var black: String?
    get() = definedExternally
    set(value) = definedExternally

  var red: String?
    get() = definedExternally
    set(value) = definedExternally

  var green: String?
    get() = definedExternally
    set(value) = definedExternally

  var yellow: String?
    get() = definedExternally
    set(value) = definedExternally

  var blue: String?
    get() = definedExternally
    set(value) = definedExternally

  var magenta: String?
    get() = definedExternally
    set(value) = definedExternally

  var cyan: String?
    get() = definedExternally
    set(value) = definedExternally

  var white: String?
    get() = definedExternally
    set(value) = definedExternally

  var brightBlack: String?
    get() = definedExternally
    set(value) = definedExternally

  var brightRed: String?
    get() = definedExternally
    set(value) = definedExternally

  var brightGreen: String?
    get() = definedExternally
    set(value) = definedExternally

  var brightYellow: String?
    get() = definedExternally
    set(value) = definedExternally

  var brightBlue: String?
    get() = definedExternally
    set(value) = definedExternally

  var brightMagenta: String?
    get() = definedExternally
    set(value) = definedExternally

  var brightCyan: String?
    get() = definedExternally
    set(value) = definedExternally

  var brightWhite: String?
    get() = definedExternally
    set(value) = definedExternally
}

external interface ILinkMatcherOptions {
  var matchIndex: Number?
    get() = definedExternally
    set(value) = definedExternally

  var validationCallback: ((uri: String, callback: (isValid: Boolean) -> Unit) -> Unit)?
    get() = definedExternally
    set(value) = definedExternally

  var tooltipCallback: ((event: MouseEvent, uri: String, location: IViewportRange) -> dynamic)?
    get() = definedExternally
    set(value) = definedExternally

  var leaveCallback: (() -> Unit)?
    get() = definedExternally
    set(value) = definedExternally

  var priority: Number?
    get() = definedExternally
    set(value) = definedExternally

  var willLinkActivate: ((event: MouseEvent, uri: String) -> Boolean)?
    get() = definedExternally
    set(value) = definedExternally
}

external interface IDisposable {
  fun dispose()
}

external interface IEvent<T, U> {
  @nativeInvoke operator fun invoke(listener: (arg1: T, arg2: U) -> Any): IDisposable
}

external interface IEvent__1<T> : IEvent<T, Unit>

external interface IMarker : IDisposable {
  var id: Number
  var isDisposed: Boolean
  var line: Number
  var onDispose: IEvent__1<Unit>
}

external interface ILocalizableStrings {
  var promptLabel: String
  var tooMuchOutput: String
}

external interface IWindowOptions {
  var restoreWin: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var minimizeWin: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var setWinPosition: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var setWinSizePixels: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var raiseWin: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var lowerWin: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var refreshWin: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var setWinSizeChars: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var maximizeWin: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var fullscreenWin: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var getWinState: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var getWinPosition: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var getWinSizePixels: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var getScreenSizePixels: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var getCellSizePixels: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var getWinSizeChars: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var getScreenSizeChars: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var getIconTitle: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var getWinTitle: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var pushTitle: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var popTitle: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var setWinLines: Boolean?
    get() = definedExternally
    set(value) = definedExternally
}

external interface KbEvent {
  var key: String
  var domEvent: KeyboardEvent
}

external interface RenderEvt {
  var start: Number
  var end: Number
}

external interface ScrollEvt {
  var cols: Number
  var rows: Number
}

open external class Terminal(options: ITerminalOptions = definedExternally) : IDisposable {
  open var element: HTMLElement?
  open var textarea: HTMLTextAreaElement?
  open var rows: Number
  open var cols: Number
  open var buffer: IBufferNamespace
  open var markers: Array<IMarker>
  open var parser: IParser
  open var unicode: IUnicodeHandling
  open var onBinary: IEvent__1<String>
  open var onCursorMove: IEvent__1<Unit>
  open var onData: IEvent__1<String>

  open fun onKey(handler: (kbEvent: KbEvent) -> Unit)

  open var onLineFeed: IEvent__1<Unit>
  open var onScroll: IEvent__1<Number>
  open var onSelectionChange: IEvent__1<Unit>
  open var onRender: IEvent__1<RenderEvt>
  open var onResize: IEvent__1<ScrollEvt>
  open var onTitleChange: IEvent__1<String>
  open var onBell: IEvent__1<Unit>

  open fun blur()

  open fun focus()

  open fun resize(columns: Number, rows: Number)

  open fun open(parent: HTMLElement)

  open fun attachCustomKeyEventHandler(customKeyEventHandler: (event: KeyboardEvent) -> Boolean)

  open fun registerLinkMatcher(
      regex: RegExp,
      handler: (event: MouseEvent, uri: String) -> Unit,
      options: ILinkMatcherOptions = definedExternally
  ): Number

  open fun deregisterLinkMatcher(matcherId: Number)

  open fun registerLinkProvider(linkProvider: ILinkProvider): IDisposable

  open fun registerCharacterJoiner(
      handler: (text: String) -> Array<Any /* JsTuple<Number, Number> */>
  ): Number

  open fun deregisterCharacterJoiner(joinerId: Number)

  open fun registerMarker(cursorYOffset: Number): IMarker?

  open fun addMarker(cursorYOffset: Number): IMarker?

  open fun hasSelection(): Boolean

  open fun getSelection(): String

  open fun getSelectionPosition(): ISelectionPosition?

  open fun clearSelection()

  open fun select(column: Number, row: Number, length: Number)

  open fun selectAll()

  open fun selectLines(start: Number, end: Number)

  override fun dispose()

  open fun scrollLines(amount: Number)

  open fun scrollPages(pageCount: Number)

  open fun scrollToTop()

  open fun scrollToBottom()

  open fun scrollToLine(line: Number)

  open fun clear()

  open fun write(data: String, callback: () -> Unit = definedExternally)

  open fun write(data: Uint8Array, callback: () -> Unit = definedExternally)

  open fun writeln(data: String, callback: () -> Unit = definedExternally)

  open fun writeln(data: Uint8Array, callback: () -> Unit = definedExternally)

  open fun writeUtf8(data: Uint8Array, callback: () -> Unit = definedExternally)

  open fun paste(data: String)

  open fun getOption(
      key:
          String /* "bellSound" | "bellStyle" | "cursorStyle" | "fontFamily" | "logLevel" | "rendererType" | "termName" | "wordSeparator" | "allowTransparency" | "cancelEvents" | "convertEol" | "cursorBlink" | "disableStdin" | "macOptionIsMeta" | "rightClickSelectsWord" | "popOnBell" | "visualBell" | "windowsMode" | "cols" | "fontSize" | "letterSpacing" | "lineHeight" | "rows" | "tabStopWidth" | "scrollback" | "fontWeight" | "fontWeightBold" */
  ): dynamic /* Any */

  open fun setOption(
      key:
          String /* "fontFamily" | "termName" | "bellSound" | "wordSeparator" | "logLevel" | "logLevel" | "logLevel" | "logLevel" | "logLevel" | "bellStyle" | "bellStyle" | "bellStyle" | "bellStyle" | "cursorStyle" | "cursorStyle" | "cursorStyle" */,
      value:
          String /* "debug" | "info" | "warn" | "error" | "off" | "none" | "visual" | "sound" | "both" | "block" | "underline" | "bar" */
  )

  open fun setOption(
      key: String /* "fontWeight" | "fontWeightBold" */,
      value:
          Any? /* "normal" | "bold" | "100" | "200" | "300" | "400" | "500" | "600" | "700" | "800" | "900" | Number? */
  )

  open fun setOption(
      key:
          String /* "allowTransparency" | "cancelEvents" | "convertEol" | "cursorBlink" | "disableStdin" | "macOptionIsMeta" | "popOnBell" | "rightClickSelectsWord" | "visualBell" | "windowsMode" */,
      value: Boolean
  )

  open fun setOption(
      key:
          String /* "fontSize" | "letterSpacing" | "lineHeight" | "tabStopWidth" | "scrollback" | "cols" | "rows" */,
      value: Number
  )

  open fun setOption(key: String /* "theme" */, value: ITheme)

  open fun setOption(key: String, value: Any)

  open fun refresh(start: Number, end: Number)

  open fun reset()

  open fun loadAddon(addon: ITerminalAddon)

  companion object {
    var strings: ILocalizableStrings
  }
}

external interface ITerminalAddon : IDisposable {
  fun activate(terminal: Terminal)
}

external interface ISelectionPosition {
  var startColumn: Number
  var startRow: Number
  var endColumn: Number
  var endRow: Number
}

external interface IViewportRange {
  var start: IViewportRangePosition
  var end: IViewportRangePosition
}

external interface IViewportRangePosition {
  var x: Number
  var y: Number
}

external interface ILinkProvider {
  fun provideLinks(bufferLineNumber: Number, callback: (links: Array<ILink>?) -> Unit)
}

external interface ILink {
  var range: IBufferRange
  var text: String
  var decorations: ILinkDecorations?
    get() = definedExternally
    set(value) = definedExternally

  fun activate(event: MouseEvent, text: String)

  val hover: ((event: MouseEvent, text: String) -> Unit)?
    get() = definedExternally

  val leave: ((event: MouseEvent, text: String) -> Unit)?
    get() = definedExternally

  val dispose: (() -> Unit)?
    get() = definedExternally
}

external interface ILinkDecorations {
  var pointerCursor: Boolean
  var underline: Boolean
}

external interface IBufferRange {
  var start: IBufferCellPosition
  var end: IBufferCellPosition
}

external interface IBufferCellPosition {
  var x: Number
  var y: Number
}

external interface IBuffer {
  var type: String /* "normal" | "alternate" */
  var cursorY: Number
  var cursorX: Number
  var viewportY: Number
  var baseY: Number
  var length: Number

  fun getLine(y: Number): IBufferLine?

  fun getNullCell(): IBufferCell
}

external interface IBufferNamespace {
  var active: IBuffer
  var normal: IBuffer
  var alternate: IBuffer
  var onBufferChange: IEvent__1<IBuffer>
}

external interface IBufferLine {
  var isWrapped: Boolean
  var length: Number

  fun getCell(x: Number, cell: IBufferCell = definedExternally): IBufferCell?

  fun translateToString(
      trimRight: Boolean = definedExternally,
      startColumn: Number = definedExternally,
      endColumn: Number = definedExternally
  ): String
}

external interface IBufferCell {
  fun getWidth(): Number

  fun getChars(): String

  fun getCode(): Number

  fun getFgColorMode(): Number

  fun getBgColorMode(): Number

  fun getFgColor(): Number

  fun getBgColor(): Number

  fun isBold(): Number

  fun isItalic(): Number

  fun isDim(): Number

  fun isUnderline(): Number

  fun isBlink(): Number

  fun isInverse(): Number

  fun isInvisible(): Number

  fun isFgRGB(): Boolean

  fun isBgRGB(): Boolean

  fun isFgPalette(): Boolean

  fun isBgPalette(): Boolean

  fun isFgDefault(): Boolean

  fun isBgDefault(): Boolean

  fun isAttributeDefault(): Boolean
}

external interface IFunctionIdentifier {
  var prefix: String?
    get() = definedExternally
    set(value) = definedExternally

  var intermediates: String?
    get() = definedExternally
    set(value) = definedExternally

  var final: String
}

external interface IParser {
  fun registerCsiHandler(
      id: IFunctionIdentifier,
      callback: (params: Array<Any /* Number | Array<Number> */>) -> Any
  ): IDisposable

  fun registerDcsHandler(
      id: IFunctionIdentifier,
      callback: (data: String, param: Array<Any /* Number | Array<Number> */>) -> Any
  ): IDisposable

  fun registerEscHandler(id: IFunctionIdentifier, handler: () -> Any): IDisposable

  fun registerOscHandler(ident: Number, callback: (data: String) -> Any): IDisposable
}

external interface IUnicodeVersionProvider {
  var version: String

  fun wcwidth(codepoint: Number): Number /* 0 | 1 | 2 */
}

external interface IUnicodeHandling {
  fun register(provider: IUnicodeVersionProvider)

  var versions: Array<String>
  var activeVersion: String
}
