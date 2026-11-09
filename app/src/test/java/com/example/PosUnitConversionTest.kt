package com.example

import com.example.data.model.ProductEntity
import com.example.data.repository.CartItem
import org.junit.Assert.assertEquals
import org.junit.Test

class PosUnitConversionTest {

    @Test
    fun testDozenAndPieceConversionAndProfit() {
        // Example from prompt: buying a box of 12 pens for 24, selling individually at 3.5 each
        val penBox = ProductEntity(
            id = 1,
            name = "أقلام بايلوت 0.7",
            purchaseUnit = "دستة (12 قلم)",
            conversionFactor = 12,
            purchaseCostPerUnit = 24.0,
            costPerPiece = 2.0,
            salePricePerPiece = 3.5,
            salePricePerDozen = 36.0,
            stockPieces = 48
        )

        assertEquals(2.0, penBox.costPerPiece, 0.001)

        // Sell 3 pieces individually
        val pieceCartItem = CartItem(
            product = penBox,
            unitType = "قطعة",
            piecesPerUnit = 1,
            quantityUnits = 3.0,
            unitPrice = 3.5,
            unitCost = 2.0
        )
        assertEquals(3, pieceCartItem.totalPieces)
        assertEquals(10.5, pieceCartItem.lineTotal, 0.001)
        assertEquals(6.0, pieceCartItem.lineCost, 0.001)
        assertEquals(4.5, pieceCartItem.lineTotal - pieceCartItem.lineCost, 0.001)

        // Sell 1 dozen directly
        val dozenCartItem = CartItem(
            product = penBox,
            unitType = "دستة",
            piecesPerUnit = 12,
            quantityUnits = 1.0,
            unitPrice = 36.0,
            unitCost = 24.0
        )
        assertEquals(12, dozenCartItem.totalPieces)
        assertEquals(36.0, dozenCartItem.lineTotal, 0.001)
        assertEquals(12.0, dozenCartItem.lineTotal - dozenCartItem.lineCost, 0.001)
    }
}
