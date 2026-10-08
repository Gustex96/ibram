package com.example.util

object CpfValidator {
    fun clean(cpf: String): String {
        return cpf.replace(Regex("[^0-9]"), "")
    }

    fun format(cpf: String): String {
        val digits = clean(cpf).take(11)
        val sb = StringBuilder()
        for (i in digits.indices) {
            sb.append(digits[i])
            if (i == 2 || i == 5) {
                sb.append(".")
            } else if (i == 8) {
                sb.append("-")
            }
        }
        return sb.toString()
    }

    fun isValid(cpf: String): Boolean {
        val cleanCpf = clean(cpf)
        if (cleanCpf.length != 11) return false

        // Check for common invalid CPFs like 11111111111
        if (cleanCpf.all { it == cleanCpf[0] }) return false

        try {
            val numbers = cleanCpf.map { it.toString().toInt() }

            // Validate first verification digit
            var sum1 = 0
            for (i in 0..8) {
                sum1 += numbers[i] * (10 - i)
            }
            var remainder1 = (sum1 * 10) % 11
            if (remainder1 == 10) remainder1 = 0
            if (remainder1 != numbers[9]) return false

            // Validate second verification digit
            var sum2 = 0
            for (i in 0..9) {
                sum2 += numbers[i] * (11 - i)
            }
            var remainder2 = (sum2 * 10) % 11
            if (remainder2 == 10) remainder2 = 0
            return remainder2 == numbers[10]
        } catch (_: Exception) {
            return false
        }
    }
}
