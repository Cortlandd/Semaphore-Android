import AppKit
import Foundation

let fileManager = FileManager.default
let repoRoot = URL(fileURLWithPath: fileManager.currentDirectoryPath)
let outputURL = repoRoot.appendingPathComponent("artwork/feature-graphic.png")
let screenshotURL = repoRoot.appendingPathComponent(
    "app/src/test/snapshots/images/com.cortlandwalker.semaphore.features.workoutlist_WorkoutListScreenSnapshotTest_WorkoutListScreen_content_state.png"
)
let iconURL = repoRoot.appendingPathComponent("app/src/main/res/drawable/web_hi_res_512.png")

let canvasWidth = 1024.0
let canvasHeight = 500.0
let canvasSize = NSSize(width: canvasWidth, height: canvasHeight)

func color(_ hex: UInt32, alpha: CGFloat = 1.0) -> NSColor {
    let r = CGFloat((hex >> 16) & 0xFF) / 255.0
    let g = CGFloat((hex >> 8) & 0xFF) / 255.0
    let b = CGFloat(hex & 0xFF) / 255.0
    return NSColor(calibratedRed: r, green: g, blue: b, alpha: alpha)
}

func roundedRect(_ rect: CGRect, radius: CGFloat) -> NSBezierPath {
    NSBezierPath(roundedRect: rect, xRadius: radius, yRadius: radius)
}

func drawGrid(in rect: CGRect, step: CGFloat, color: NSColor) {
    color.setStroke()
    let path = NSBezierPath()
    path.lineWidth = 1

    var x = rect.minX
    while x <= rect.maxX {
        path.move(to: CGPoint(x: x, y: rect.minY))
        path.line(to: CGPoint(x: x, y: rect.maxY))
        x += step
    }

    var y = rect.minY
    while y <= rect.maxY {
        path.move(to: CGPoint(x: rect.minX, y: y))
        path.line(to: CGPoint(x: rect.maxX, y: y))
        y += step
    }

    path.stroke()
}

func drawShadowedCard(rect: CGRect, radius: CGFloat, fill: NSColor, shadowColor: NSColor, shadowBlur: CGFloat, shadowOffset: CGSize) {
    let shadow = NSShadow()
    shadow.shadowColor = shadowColor
    shadow.shadowBlurRadius = shadowBlur
    shadow.shadowOffset = shadowOffset
    NSGraphicsContext.saveGraphicsState()
    shadow.set()
    fill.setFill()
    roundedRect(rect, radius: radius).fill()
    NSGraphicsContext.restoreGraphicsState()
}

func drawText(_ text: String, rect: CGRect, font: NSFont, color: NSColor, alignment: NSTextAlignment = .left) {
    let paragraph = NSMutableParagraphStyle()
    paragraph.alignment = alignment
    let attributes: [NSAttributedString.Key: Any] = [
        .font: font,
        .foregroundColor: color,
        .paragraphStyle: paragraph
    ]
    text.draw(in: rect, withAttributes: attributes)
}

let bitmap = NSBitmapImageRep(
    bitmapDataPlanes: nil,
    pixelsWide: Int(canvasWidth),
    pixelsHigh: Int(canvasHeight),
    bitsPerSample: 8,
    samplesPerPixel: 4,
    hasAlpha: true,
    isPlanar: false,
    colorSpaceName: .deviceRGB,
    bytesPerRow: 0,
    bitsPerPixel: 0
)!

NSGraphicsContext.saveGraphicsState()
NSGraphicsContext.current = NSGraphicsContext(bitmapImageRep: bitmap)

let fullRect = CGRect(origin: .zero, size: canvasSize)

color(0xF7F7FA).setFill()
fullRect.fill()
drawGrid(in: fullRect, step: 48, color: color(0xD9D9E3, alpha: 0.55))

let gradientBlob1 = NSGradient(colors: [color(0x6A5ACD, alpha: 0.16), color(0x6A5ACD, alpha: 0.0)])!
gradientBlob1.draw(in: NSBezierPath(ovalIn: CGRect(x: -60, y: 250, width: 280, height: 280)), relativeCenterPosition: .zero)

let gradientBlob2 = NSGradient(colors: [color(0x6A5ACD, alpha: 0.18), color(0x6A5ACD, alpha: 0.0)])!
gradientBlob2.draw(in: NSBezierPath(ovalIn: CGRect(x: 780, y: 40, width: 280, height: 280)), relativeCenterPosition: .zero)

let accentGradient = NSGradient(colors: [color(0x7B6CDE), color(0x5D4FC1)])!
let iconCardRect = CGRect(x: 68, y: 258, width: 168, height: 168)
drawShadowedCard(
    rect: iconCardRect,
    radius: 42,
    fill: color(0x6A5ACD),
    shadowColor: color(0x6A5ACD, alpha: 0.28),
    shadowBlur: 28,
    shadowOffset: CGSize(width: 0, height: -6)
)
NSGraphicsContext.saveGraphicsState()
roundedRect(iconCardRect, radius: 42).addClip()
accentGradient.draw(in: iconCardRect, angle: 270)
NSGraphicsContext.restoreGraphicsState()

if let iconImage = NSImage(contentsOf: iconURL) {
    let iconRect = CGRect(x: iconCardRect.minX + 26, y: iconCardRect.minY + 26, width: 116, height: 116)
    iconImage.draw(in: iconRect)
}

let titleFont = NSFont.systemFont(ofSize: 58, weight: .heavy)
let subtitleFont = NSFont.systemFont(ofSize: 23, weight: .regular)
let chipFont = NSFont.systemFont(ofSize: 18, weight: .semibold)
let headlineColor = color(0x17171C)
let secondaryColor = color(0x6F6F78)

drawText(
    "Semaphore",
    rect: CGRect(x: 270, y: 330, width: 360, height: 80),
    font: titleFont,
    color: headlineColor
)

drawText(
    "Workout timers with routines,\nbackground playback, and GIF cues.",
    rect: CGRect(x: 274, y: 248, width: 370, height: 68),
    font: subtitleFont,
    color: secondaryColor
)

func drawChip(_ text: String, rect: CGRect, fill: NSColor, foreground: NSColor) {
    fill.setFill()
    roundedRect(rect, radius: 18).fill()
    drawText(text, rect: rect.insetBy(dx: 16, dy: 9), font: chipFont, color: foreground, alignment: .center)
}

drawChip("Play All", rect: CGRect(x: 272, y: 170, width: 124, height: 48), fill: color(0x6A5ACD), foreground: .white)
drawChip("Background Timers", rect: CGRect(x: 408, y: 170, width: 202, height: 48), fill: color(0xECE9FB), foreground: color(0x5B4EC0))
drawChip("One-Time Ad Removal", rect: CGRect(x: 272, y: 112, width: 230, height: 48), fill: .white, foreground: headlineColor)

let motifRects = [
    CGRect(x: 84, y: 88, width: 138, height: 20),
    CGRect(x: 108, y: 52, width: 194, height: 20),
    CGRect(x: 84, y: 16, width: 138, height: 20)
]
for rect in motifRects {
    drawShadowedCard(
        rect: rect,
        radius: 10,
        fill: color(0x17171C),
        shadowColor: color(0x17171C, alpha: 0.18),
        shadowBlur: 10,
        shadowOffset: CGSize(width: 0, height: -2)
    )
}

let frameRect = CGRect(x: 650, y: 44, width: 320, height: 412)
drawShadowedCard(
    rect: frameRect,
    radius: 42,
    fill: .white,
    shadowColor: color(0x6A5ACD, alpha: 0.16),
    shadowBlur: 30,
    shadowOffset: CGSize(width: 0, height: -8)
)

let innerFrame = frameRect.insetBy(dx: 10, dy: 10)
color(0x6A5ACD, alpha: 0.95).setStroke()
let borderPath = roundedRect(innerFrame, radius: 34)
borderPath.lineWidth = 4
borderPath.stroke()

if let screenshot = NSImage(contentsOf: screenshotURL) {
    NSGraphicsContext.saveGraphicsState()
    roundedRect(innerFrame.insetBy(dx: 6, dy: 6), radius: 28).addClip()

    let sourceSize = screenshot.size
    let targetRect = innerFrame.insetBy(dx: 6, dy: 6)
    let sourceAspect = sourceSize.width / sourceSize.height
    let targetAspect = targetRect.width / targetRect.height

    var drawRect = targetRect
    if sourceAspect > targetAspect {
        let scaledWidth = targetRect.height * sourceAspect
        drawRect.origin.x -= (scaledWidth - targetRect.width) / 2
        drawRect.size.width = scaledWidth
    } else {
        let scaledHeight = targetRect.width / sourceAspect
        drawRect.origin.y -= (scaledHeight - targetRect.height) / 2
        drawRect.size.height = scaledHeight
    }

    screenshot.draw(in: drawRect)
    NSGraphicsContext.restoreGraphicsState()
}

let playButtonRect = CGRect(x: 820, y: 42, width: 104, height: 104)
drawShadowedCard(
    rect: playButtonRect,
    radius: 52,
    fill: color(0x6A5ACD),
    shadowColor: color(0x6A5ACD, alpha: 0.25),
    shadowBlur: 18,
    shadowOffset: CGSize(width: 0, height: -4)
)

let triangle = NSBezierPath()
triangle.move(to: CGPoint(x: playButtonRect.minX + 42, y: playButtonRect.minY + 29))
triangle.line(to: CGPoint(x: playButtonRect.minX + 42, y: playButtonRect.minY + 75))
triangle.line(to: CGPoint(x: playButtonRect.minX + 77, y: playButtonRect.minY + 52))
triangle.close()
NSColor.white.setFill()
triangle.fill()

NSGraphicsContext.restoreGraphicsState()

guard let pngData = bitmap.representation(using: .png, properties: [:]) else {
    fatalError("Could not encode PNG")
}

try pngData.write(to: outputURL)
print("Wrote \(outputURL.path)")
