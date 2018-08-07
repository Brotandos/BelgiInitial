import BelgiUtil.ATTR_LABEL_CONSTRAINT_TO_EDGE
import BelgiUtil.ATTR_LABEL_CONSTRAINT_TO_ID
import BelgiUtil.ATTR_LABEL_ID
import BelgiUtil.ATTR_LABEL_MARGIN
import BelgiUtil.TAG_NAME_BELGI
import org.w3c.dom.Element
import org.w3c.dom.Node

fun main(args: Array<String>) {
    val idBelgi1 = 1
    val idBelgi2 = 2

    val start = Edge.START
    val top = Edge.TOP
    val end = Edge.END
    val bottom = Edge.BOTTOM

    val markup = """
                $empty
        ${idBelgi2.start by 10}${Belgi(idBelgi1)}${idBelgi2.end by 10}                      ${idBelgi1.top by 10}
                $empty                                               ${idBelgi1.start by 10}${Belgi(idBelgi2)}${idBelgi1.end by 10}
                                                                                                $empty
    """.trimIndent()

    val belgiList = markup.toBelgiList()
    belgiList.forEach { println(it.id) }
}

fun String.toBelgiList(): List<Belgi> {
    fun addHorizontalConstraints(i: Int, belgiList: List<Belgi>, belgiNode: Node) {
        fun parseConstraint(edge: Edge, constraintNode: Node?) {
            fun addConstraint(edge: Edge, constraintElement: Element) {
                val toId = constraintElement.getAttribute(ATTR_LABEL_CONSTRAINT_TO_ID).toInt()
                if (toId == -1) return

                val toEdge = asEdge(constraintElement.getAttribute(ATTR_LABEL_CONSTRAINT_TO_EDGE).toInt())
                val margin = constraintElement.getAttribute(ATTR_LABEL_MARGIN).toInt()

                val to = belgiList.find { it.id == toId }
                if (to != null)
                    belgiList[i].constraints += SubjectiveConstraint(edge, to, toEdge, margin)
                else
                    RuntimeException("There's no such belgi with id = $toId")

                constraintElement.parentNode.removeChild(constraintElement)
            }

            when {
                constraintNode == null -> RuntimeException("Need to add start constraint") // TODO custom exception
                constraintNode.nodeType == Node.ELEMENT_NODE -> addConstraint(edge, constraintNode as Element)
                else -> RuntimeException("Unknown node type")
            }
        }

        parseConstraint(Edge.START, belgiNode.previousSibling)
        parseConstraint(Edge.END, belgiNode.nextSibling)
    }

    val belgiList = mutableListOf<Belgi>()
    val xmlDocument = BelgiUtil.buildXmlDocument(this)
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