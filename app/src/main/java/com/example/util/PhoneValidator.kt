package com.example.util

object PhoneValidator {
    fun clean(phone: String): String {
        return phone.replace(Regex("[^0-9]"), "").take(11)
    }

    fun isValid(phone: String): Boolean {
        val digits = clean(phone)
        return digits.length in 10..11
    }

    /**
     * Formatação automática de telefone brasileiro:
     * - Até 2 dígitos: (XX
     * - 3 a 6 dígitos: (XX) XXXX
     * - 7 a 10 dígitos (Telefone Fixo): (XX) XXXX-XXXX
     * - 11 dígitos (Telefone Celular): (XX) XXXXX-XXXX
     */
    fun format(phone: String): String {
        val digits = clean(phone)
        val len = digits.length
        if (len == 0) return ""
        if (len <= 2) return "($digits"

        val isMobile = digits.length > 2 && digits[2] == '9'
        return if (isMobile) {
            if (len <= 7) {
                "(${digits.substring(0, 2)}) ${digits.substring(2)}"
            } else {
                "(${digits.substring(0, 2)}) ${digits.substring(2, 7)}-${digits.substring(7)}"
            }
        } else {
            if (len <= 6) {
                "(${digits.substring(0, 2)}) ${digits.substring(2)}"
            } else {
                "(${digits.substring(0, 2)}) ${digits.substring(2, 6)}-${digits.substring(6)}"
            }
        }
    }

    /**
     * Formata entrada de telefone durante digitação em tempo real,
     * tratando backspace para permitir apagar sem travar em caracteres especiais.
     */
    fun formatInput(input: String, previousText: String = ""): String {
        val cleanInput = clean(input)
        val cleanPrev = clean(previousText)

        val effective = if (input.length < previousText.length && cleanInput == cleanPrev && cleanInput.isNotEmpty()) {
            cleanInput.dropLast(1)
        } else {
            cleanInput
        }

        return format(effective)
    }
}
