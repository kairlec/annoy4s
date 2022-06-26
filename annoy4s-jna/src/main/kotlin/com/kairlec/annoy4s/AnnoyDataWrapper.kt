package com.kairlec.annoy4s

object IntConverter : AnnoyDataConverter<Int> {
    override fun save(data: Int): String {
        return data.toString()
    }

    override fun load(data: String): Int {
        return data.toInt()
    }
}

object CharConverter : AnnoyDataConverter<Char> {
    override fun save(data: Char): String {
        return data.toString()
    }

    override fun load(data: String): Char {
        return data[0]
    }
}

object CharSequenceConverter : AnnoyDataConverter<CharSequence> {
    override fun save(data: CharSequence): String {
        return data.toString()
    }

    override fun load(data: String): CharSequence {
        return data
    }
}

object StringConverter : AnnoyDataConverter<String> {
    override fun save(data: String): String {
        return data
    }

    override fun load(data: String): String {
        return data
    }
}
