import BelgiUtil.ATTR_LABEL_ID
import BelgiUtil.TAG_NAME_BELGI
import org.w3c.dom.Element
import org.w3c.dom.Node

fun main(args: Array<String>) {
    val markup = """
                $empty
        $empty${Belgi()}$empty          $empty
                $empty         $empty${Belgi()}$empty
                                        $empty
    """.trimIndent()

    println(markup)

    val list = mutableListOf(empty, empty, empty, empty)
    list[Edge.START] = empty

    /*val belgiList = markup.toBelgiList()
    belgiList.forEach { println(it.id) }*/
}

fun String.toBelgiList(): List<Belgi> {
    val belgiList = mutableListOf<Belgi>()
    val xmlDocument = BelgiUtil.buildXmlDocument(this)
    val belgiNodeList = xmlDocument.getElementsByTagName(TAG_NAME_BELGI)
    // TODO handle if elements don't exist

    for (i in 0 until belgiNodeList.length) {
        val belgiNode = belgiNodeList.item(i)
        if (belgiNode.nodeType == Node.ELEMENT_NODE)
            belgiList += belgiNode.toBelgi()
        // TODO handle else case
    }

    return belgiList
}

fun Node.toBelgi(): Belgi {
    val belgiElement = this as Element
    val id = belgiElement.getAttribute(ATTR_LABEL_ID).toInt()
    return Belgi(id)
}