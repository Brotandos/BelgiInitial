fun main(args: Array<String>) {

    val belgi1 = Belgi(1)
    val belgi2 = Belgi(2)

    val b1 = belgi1
    val b2 = belgi2
    val markup = """
                      *
        ${b2 - 10}$belgi1${b2 + 10}            ${b1 + 10}
                  ${b2 + 10}           ${b1 + 10}$belgi2 *
                                                    *
    """.trimIndent()

    val belgiList = BelgiUtil.toBelgiList(markup)

    belgiList.forEach { belgi ->
        println(belgi.id)
        belgi.qatnasList.forEach {
            println("${it.to.first.id}, ${it.fromEdge}. ${it.margin}")
        }
    }
}