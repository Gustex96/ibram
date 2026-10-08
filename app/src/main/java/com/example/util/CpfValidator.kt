package com.example.util

object CpfValidator {
    fun clean(document: String): String {
        return document.replace(Regex("[^0-9]"), "")
    }

    fun isCnpj(document: String): Boolean {
        return clean(document).length > 11
    }

    fun getDocumentType(document: String): String {
        return if (isCnpj(document)) "CNPJ" else "CPF"
    }

    /**
     * Formatação dinâmica:
     * - Até 11 dígitos: formata como CPF (000.000.000-00)
     * - De 12 a 14 dígitos: formata automaticamente como CNPJ (00.000.000/0000-00)
     */
    fun format(document: String): String {
        val digits = clean(document).take(14)
        return if (digits.length <= 11) {
            formatCpf(digits)
        } else {
            formatCnpj(digits)
        }
    }

    fun formatCpf(digits: String): String {
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

    fun formatCnpj(digits: String): String {
        val sb = StringBuilder()
        for (i in digits.indices) {
            sb.append(digits[i])
            if (i == 1 || i == 4) {
                sb.append(".")
            } else if (i == 7) {
                sb.append("/")
            } else if (i == 11) {
                sb.append("-")
            }
        }
        return sb.toString()
    }

    /**
     * Validação de CPF (11 dígitos com cálculo de dígitos verificadores mod 11).
     */
    fun isValidCpf(cpf: String): Boolean {
        val cleanCpf = clean(cpf)
        if (cleanCpf.length != 11) return false
        if (cleanCpf.all { it == cleanCpf[0] }) return false

        return try {
            val numbers = cleanCpf.map { it.toString().toInt() }

            var sum1 = 0
            for (i in 0..8) {
                sum1 += numbers[i] * (10 - i)
            }
            var remainder1 = (sum1 * 10) % 11
            if (remainder1 == 10) remainder1 = 0
            if (remainder1 != numbers[9]) return false

            var sum2 = 0
            for (i in 0..9) {
                sum2 += numbers[i] * (11 - i)
            }
            var remainder2 = (sum2 * 10) % 11
            if (remainder2 == 10) remainder2 = 0
            remainder2 == numbers[10]
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Validação de CNPJ (14 dígitos com cálculo de dígitos verificadores mod 11).
     */
    fun isValidCnpj(cnpj: String): Boolean {
        val cleanCnpj = clean(cnpj)
        if (cleanCnpj.length != 14) return false
        if (cleanCnpj.all { it == cleanCnpj[0] }) return false

        return try {
            val numbers = cleanCnpj.map { it.toString().toInt() }

            val weights1 = intArrayOf(5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
            var sum1 = 0
            for (i in 0..11) {
                sum1 += numbers[i] * weights1[i]
            }
            var remainder1 = sum1 % 11
            val digit1 = if (remainder1 < 2) 0 else 11 - remainder1
            if (numbers[12] != digit1) return false

            val weights2 = intArrayOf(6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
            var sum2 = 0
            for (i in 0..12) {
                sum2 += numbers[i] * weights2[i]
            }
            var remainder2 = sum2 % 11
            val digit2 = if (remainder2 < 2) 0 else 11 - remainder2
            numbers[13] == digit2
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Validação dinâmica:
     * Valida como CPF se tiver 11 dígitos, ou como CNPJ se tiver 14 dígitos.
     */
    fun isValid(document: String): Boolean {
        val cleanDoc = clean(document)
        return when (cleanDoc.length) {
            11 -> isValidCpf(cleanDoc)
            14 -> isValidCnpj(cleanDoc)
            else -> false
        }
    }
}
