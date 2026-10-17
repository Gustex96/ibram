package com.example.util

object CpfValidator {
    fun isCnpj(document: String): Boolean {
        val clean = document.filter { it.isDigit() }
        return clean.length == 14
    }

    fun formatCpfOrCnpj(document: String): String {
        val digits = document.filter { it.isDigit() }
        return when (digits.length) {
            11 -> "${digits.substring(0, 3)}.${digits.substring(3, 6)}.${digits.substring(6, 9)}-${digits.substring(9, 11)}"
            14 -> "${digits.substring(0, 2)}.${digits.substring(2, 5)}.${digits.substring(5, 8)}/${digits.substring(8, 12)}-${digits.substring(12, 14)}"
            else -> document
        }
    }
}
