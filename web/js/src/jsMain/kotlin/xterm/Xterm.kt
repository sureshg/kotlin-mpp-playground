@file:JsModule("@xterm/xterm")
@file:JsNonModule

package xterm

import org.khronos.webgl.Uint8Array
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.WheelEvent

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

  var convertEol: Boolean?
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

  var cursorInactiveStyle: String? /* "outline" | "block" | "bar" | "underline" | "none" */
    get() = definedExternally
    set(value) = definedExternally

  var customGlyphs: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var disableStdin: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var documentOverride: Any?
    get() = definedExternally
    set(value) = definedExternally

  var drawBoldTextInBrightColors: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var fastScrollModifier: String? /* "none" | "alt" | "ctrl" | "shift" */
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

  var ignoreBracketedPasteMode: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var letterSpacing: Number?
    get() = definedExternally
    set(value) = definedExternally

  var lineHeight: Number?
    get() = definedExternally
    set(value) = definedExternally

  var linkHandler: ILinkHandler?
    get() = definedExternally
    set(value) = definedExternally

  var logLevel: String? /* "trace" | "debug" | "info" | "warn" | "error" | "off" */
    get() = definedExternally
    set(value) = definedExternally

  var logger: ILogger?
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

  var rescaleOverlappingGlyphs: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var rightClickSelectsWord: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var screenReaderMode: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var scrollback: Number?
    get() = definedExternally
    set(value) = definedExternally

  var scrollOnUserInput: Boolean?
    get() = definedExternally
    set(value) = definedExternally

  var scrollSensitivity: Number?
    get() = definedExternally
    set(value) = definedExternally

  var smoothScrollDuration: Number?
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

  var windowsPty: IWindowsPty?
    get() = definedExternally
    set(value) = definedExternally

  var wordSeparator: String?
    get() = definedExternally
    set(value) = definedExternally

  var windowOptions: IWindowOptions?
    get() = definedExternally
    set(value) = definedExternally

  var overviewRulerWidth: Number?
    get() = definedExternally
    set(value) = definedExternally
}

external interface ITerminalInitOnlyOptions {
  var cols: Number?
    get() = definedExternally
    set(value) = definedExternally

  var rows: Number?
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

  var selectionBackground: String?
    get() = definedExternally
    set(value) = definedExternally

  var selectionForeground: String?
    get() = definedExternally
    set(value) = definedExternally

  var selectionInactiveBackground: String?
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

  var extendedAnsi: Array<String>?
    get() = definedExternally
    set(value) = definedExternally
}

external interface IWindowsPty {
  var backend: String? /* "conpty" | "winpty" */
    get() = definedExternally
    set(value) = definedExternally

  var buildNumber: Number?
    get() = definedExternally
    set(value) = definedExternally
}

external interface ILogger {
  fun trace(message: String, vararg args: Any)

  fun debug(message: String, vararg args: Any)

  fun info(message: String, vararg args: Any)

  fun warn(message: String, vararg args: Any)

  fun error(message: String, vararg args: Any)

  fun error(message: Error, vararg args: Any)
}

external interface IDisposable {
  fun dispose()
}

external interface IEvent<T, U> {
  @nativeInvoke operator fun invoke(listener: (arg1: T, arg2: U) -> Any): IDisposable
}

external interface IEvent__1<T> : IEvent<T, Unit>

external interface IMarker : IDisposableWithEvent {
  val id: Number
  val line: Number
}

external interface IDisposableWithEvent : IDisposable {
  var onDispose: IEvent__1<Unit>
  val isDisposed: Boolean
}

external interface IDecoration : IDisposableWithEvent {
  val marker: IMarker
  val onRender: IEvent__1<HTMLElement>
  var element: HTMLElement?
  var options: Pick<IDecorationOptions, String /* "overviewRulerOptions" */>
}

external interface IDecorationOverviewRulerOptions {
  var color: String
  var position: String? /* "left" | "center" | "right" | "full" */
    get() = definedExternally
    set(value) = definedExternally
}

external interface IDecorationOptions {
  val marker: IMarker
  val anchor: String? /* "right" | "left" */
    get() = definedExternally

  val x: Number?
    get() = definedExternally

  val width: Number?
    get() = definedExternally

  val height: Number?
    get() = definedExternally

  val backgroundColor: String?
    get() = definedExternally

  val foregroundColor: String?
    get() = definedExternally

  val layer: String? /* "bottom" | "top" */
    get() = definedExternally

  var overviewRulerOptions: IDecorationOverviewRulerOptions?
    get() = definedExternally
    set(value) = definedExternally
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

external interface `T$0` {
  var key: String
  var domEvent: KeyboardEvent
}

external interface `T$1` {
  var start: Number
  var end: Number
}

external interface `T$2` {
  var cols: Number
  var rows: Number
}

external open class Terminal(
    options: ITerminalOptions /* ITerminalOptions & ITerminalInitOnlyOptions */ = definedExternally
) : IDisposable {
  open val element: HTMLElement?
  open val textarea: HTMLTextAreaElement?
  open val rows: Number
  open val cols: Number
  open val buffer: IBufferNamespace
  open val markers: Array<IMarker>
  open val parser: IParser
  open val unicode: IUnicodeHandling
  open val modes: IModes
  open var options: ITerminalOptions
  open var onBell: IEvent__1<Unit>
  open var onBinary: IEvent__1<String>
  open var onCursorMove: IEvent__1<Unit>
  open var onData: IEvent__1<String>
  open var onKey: IEvent__1<`T$0`>
  open var onLineFeed: IEvent__1<Unit>
  open var onRender: IEvent__1<`T$1`>
  open var onWriteParsed: IEvent__1<Unit>
  open var onResize: IEvent__1<`T$2`>
  open var onScroll: IEvent__1<Number>
  open var onSelectionChange: IEvent__1<Unit>
  open var onTitleChange: IEvent__1<String>

  open fun blur()

  open fun focus()

  open fun input(data: String, wasUserInput: Boolean = definedExternally)

  open fun resize(columns: Number, rows: Number)

  open fun open(parent: HTMLElement)

  open fun attachCustomKeyEventHandler(customKeyEventHandler: (event: KeyboardEvent) -> Boolean)

  open fun attachCustomWheelEventHandler(customWheelEventHandler: (event: WheelEvent) -> Boolean)

  open fun registerLinkProvider(linkProvider: ILinkProvider): IDisposable

  open fun registerCharacterJoiner(
      handler: (text: String) -> Array<Any /* JsTuple<Number, Number> */>
  ): Number

  open fun deregisterCharacterJoiner(joinerId: Number)

  open fun registerMarker(cursorYOffset: Number = definedExternally): IMarker

  open fun registerDecoration(decorationOptions: IDecorationOptions): IDecoration?

  open fun hasSelection(): Boolean

  open fun getSelection(): String

  open fun getSelectionPosition(): IBufferRange?

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

  open fun write(data: String)

  open fun write(data: Uint8Array, callback: () -> Unit = definedExternally)

  open fun write(data: Uint8Array)

  open fun writeln(data: String, callback: () -> Unit = definedExternally)

  open fun writeln(data: String)

  open fun writeln(data: Uint8Array, callback: () -> Unit = definedExternally)

  open fun writeln(data: Uint8Array)

  open fun paste(data: String)

  open fun refresh(start: Number, end: Number)

  open fun clearTextureAtlas()

  open fun reset()

  open fun loadAddon(addon: ITerminalAddon)

  companion object {
    var strings: ILocalizableStrings
  }
}

external interface ITerminalAddon : IDisposable {
  fun activate(terminal: Terminal)
}

external interface IViewportRange {
  var start: IViewportRangePosition
  var end: IViewportRangePosition
}

external interface IViewportRangePosition {
  var x: Number
  var y: Number
}

external interface ILinkHandler {
  fun activate(event: MouseEvent, text: String, range: IBufferRange)

  val hover: ((event: MouseEvent, text: String, range: IBufferRange) -> Unit)?
  val leave: ((event: MouseEvent, text: String, range: IBufferRange) -> Unit)?
  var allowNonHttpProtocols: Boolean?
    get() = definedExternally
    set(value) = definedExternally
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
  val leave: ((event: MouseEvent, text: String) -> Unit)?
  val dispose: (() -> Unit)?
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
  val type: String /* "normal" | "alternate" */
  val cursorY: Number
  val cursorX: Number
  val viewportY: Number
  val baseY: Number
  val length: Number

  fun getLine(y: Number): IBufferLine?

  fun getNullCell(): IBufferCell
}

external interface IBufferElementProvider {
  fun provideBufferElements(): dynamic /* DocumentFragment | HTMLElement */
}

external interface IBufferNamespace {
  val active: IBuffer
  val normal: IBuffer
  val alternate: IBuffer
  var onBufferChange: IEvent__1<IBuffer>
}

external interface IBufferLine {
  val isWrapped: Boolean
  val length: Number

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

  fun isStrikethrough(): Number

  fun isOverline(): Number

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
  val version: String

  fun wcwidth(codepoint: Number): Number /* 0 | 1 | 2 */

  fun charProperties(codepoint: Number, preceding: Number): Number
}

external interface IUnicodeHandling {
  fun register(provider: IUnicodeVersionProvider)

  val versions: Array<String>
  var activeVersion: String
}

external interface IModes {
  val applicationCursorKeysMode: Boolean
  val applicationKeypadMode: Boolean
  val bracketedPasteMode: Boolean
  val insertMode: Boolean
  val mouseTrackingMode: String /* "none" | "x10" | "vt200" | "drag" | "any" */
  val originMode: Boolean
  val reverseWraparoundMode: Boolean
  val sendFocusMode: Boolean
  val wraparoundMode: Boolean
}
