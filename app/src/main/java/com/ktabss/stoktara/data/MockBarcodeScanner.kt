package com.ktabss.stoktara.data

/**
 * Stand-in for the real camera scanner (CameraX + ML Kit later).
 * "Scanning" just returns the next barcode from a HashMap of barcode -> SKU,
 * so the whole Scan flow can be demoed and tested without a camera.
 *
 * To swap in the real thing: delete nextCode() and feed the decoded barcode string
 * into skuFor() instead. Nothing else in the Scan flow changes.
 */
object MockBarcodeScanner {
    private val barcodeToSku: HashMap<String, String> = hashMapOf(
        "4800010000011" to "BKY-0001", // All-Purpose Flour
        "4800010000028" to "BKY-0002", // White Sugar
        "4800010000035" to "BKY-0003", // Almond Milk
        "4800010000042" to "BKY-0004", // Eggs (Tray)
        "4800010000059" to "BKY-0005", // Instant Yeast
        "4800010000066" to "BKY-0006", // Bakery Boxes (Small)
        "4800010000073" to "BKY-0007", // Unsalted Butter
        "4800010000080" to "BKY-0008", // Bottled Water 500mL
    )

    private val codes = barcodeToSku.keys.sorted()
    private var cursor = 0

    /** Pretend the camera just read a barcode; cycles through the mock codes. */
    fun nextCode(): String = codes[cursor++ % codes.size]

    /** barcode value -> SKU, or null if the code isn't in the map. */
    fun skuFor(barcode: String): String? = barcodeToSku[barcode.trim()]
}
