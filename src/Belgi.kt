import BelgiUtil.ATTR_LABEL_ID
import BelgiUtil.TAG_MARKUP_END
import BelgiUtil.TAG_MARKUP_START
import BelgiUtil.TAG_NAME_BELGI
import BelgiUtil.createId
import org.w3c.dom.Document
import org.xml.sax.InputSource
import java.io.StringReader
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.math.absoluteValue

object BelgiUtil {
    const val TAG_NAME_MARKUP = "BelgiMarkup"
    const val TAG_MARKUP_START = "<$TAG_NAME_MARKUP>"
    const val TAG_MARKUP_END = "</$TAG_NAME_MARKUP>"
    const val TAG_NAME_BELGI = "Belgi"
    const val TAG_NAME_CONSTRAINT = "Constraint"
    private const val TAG_NAME_LINE = "Line"

    const val ATTR_LABEL_ID = "id"
    const val ATTR_LABEL_CONSTRAINT_TO_ID = "to_id"
    const val ATTR_LABEL_CONSTRAINT_TO_EDGE = "to_edge"
    const val ATTR_LABEL_MARGIN = "margin"
    const val ATTR_VALUE_EMPTY_CONSTRAINT = -1

    val createId: Int get() = Random().nextInt().absoluteValue + 1

    fun buildXmlDocument(markup: String): Document {
        val linedMarkup = wrapEachLine(markup)
        val inputSource = InputSource(StringReader(linedMarkup.wellFormedXml))
        val documentBuilderFactory = DocumentBuilderFactory.newInstance()
        documentBuilderFactory.isIgnoringElementContentWhitespace = true
        return documentBuilderFactory.newDocumentBuilder().parse(inputSource)
    }

    fun wrapEachLine(markup: String): String {
        val stringBuilder = StringBuilder()
        markup.split('\n').forEach {
            stringBuilder.append("<$TAG_NAME_LINE>$it</$TAG_NAME_LINE>\n")
        }
        println(stringBuilder)
        return stringBuilder.toString()
    }
}

val String.wellFormedXml: String get() = """
        $TAG_MARKUP_START
            $this
        $TAG_MARKUP_END
    """.trimIndent()

open class Belgi(val id: Int) {

    val constraints = mutableListOf<SubjectiveConstraint>()

    constructor() : this(createId)

    private val idXmlAttr: String get() = "$ATTR_LABEL_ID=\"$id\""

    override fun toString() = """
        $<$TAG_NAME_BELGI $idXmlAttr />
    """.trimIndent()
}

enum class Edge(val serializedValue: Int) {
    START(0),
    TOP(1),
    END(2),
    BOTTOM(3);
}

fun asEdge(value: Int): Edge {
    if (value < 0 || value > 3) RuntimeException("Invalid edge value: $value")
    return Edge.values().find { it.serializedValue == value }!! // TODO fix
}

open class SubjectiveConstraint(val fromEdge: Edge,
                                val to: Pair<Belgi, Edge>,
                                val margin: Int = 0) {

    constructor(fromEdge: Edge, toBelgi: Belgi, toEdge: Edge, margin: Int = 0) : this(fromEdge, toBelgi to toEdge, margin)

    override fun toString(): String {
        return super.toString()
    }
}

operator fun List<Any>.get(edge: Edge) = this[edge.serializedValue]
operator fun MutableList<Any>.set(edge: Edge, a: Any) {
    this[edge.serializedValue] = a
}