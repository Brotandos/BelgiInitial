import BelgiUtil.getBelgiToConstraintsList

fun main(args: Array<String>) {

    val belgi1 = Belgi(1)
    val belgi2 = Belgi(2)

    val b1 = belgi1
    val b2 = belgi2
    val markup = """
                      *
        ${b2 - 10}$belgi1${b2 + 10}               ${b1 + 10}
                  ${b2 + 10}              ${b1 + 10}$belgi2 *
                                                        *
    """.trimIndent()

    val belgiList = toBelgiList(markup)
    belgiList.forEach { println(it) }
}

fun toBelgiList(markup: String): List<Belgi> {
    val listOfNormalizedJsonArray = BelgiUtil.wrapToBelgiNorm(markup)
    val belgiToConstraintsList = getBelgiToConstraintsList(listOfNormalizedJsonArray)
    val belgiList = mutableListOf<Belgi>()
    belgiToConstraintsList.forEach { belgiToConstraints ->
        val belgi = belgiToConstraints.first

        belgiToConstraints.second.forEach { constraint ->
            val toBelgi = belgiToConstraintsList.find { it.first.id == constraint.toBelgiId }?.first
            if (toBelgi != null)
                belgi.qatnasList += SubjectiveQatnas(constraint.fromEdge, toBelgi, constraint.toEdge, constraint.margin)
            else
                throw RuntimeException("There is no belgi inside markup with id ${constraint.toBelgiId}")
        }

        belgiList += belgi
    }
    return belgiList
}