import BelgiUtil.ATTR_LABEL_ID
import org.w3c.dom.Element
import org.w3c.dom.Node

fun main(args: Array<String>) {

    val belgi1 = Belgi(1)
    val belgi2 = Belgi(2)

    val markup = """
                          *
        ${belgi2 - 10}$belgi1${belgi2 + 10}               ${belgi1 + 10}
                  ${belgi2 + 10}              ${belgi1 + 10}$belgi2 *
                                                                *
    """.trimIndent()

    val belgiList = markup.toBelgiList()
    belgiList.forEach { println(it.id) }
}

fun String.toBelgiList(): List<Belgi> {
    val belgiList = mutableListOf<Belgi>()
    val normalizedJson = BelgiUtil.wrapToBelgiNorm(this)
    val belgiNodeList = xmlDocument.getElementsByTagName(TAG_NAME_BELGI)
    // TODO handle if elements don't exist

    // collect all belgi from markup
    for (i in 0 until belgiNodeList.length) {
        val belgiNode = belgiNodeList.item(i)
        if (belgiNode.nodeType == Node.ELEMENT_NODE)
            belgiList += belgiNode.toBelgi()
        else
            RuntimeException("Unknown node type") // TODO make custom exception
    }

    for (i in 0 until belgiNodeList.length)
        addHorizontalConstraints(i, belgiList, belgiNodeList.item(i))

    belgiList.forEach {
        println("""
            id: ${it.id}, constraints: ${it.constraints}
        """.trimIndent())
    }

    return belgiList
}

fun Node.toBelgi(): Belgi {
    val belgiElement = this as Element
    val id = belgiElement.getAttribute(ATTR_LABEL_ID).toInt()
    return Belgi(id)
}