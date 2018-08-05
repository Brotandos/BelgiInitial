import BelgiUtil.ATTR_EQUALS
import BelgiUtil.ATTR_LABEL_ID
import BelgiUtil.ATTR_LABEL_CONSTRAINT_TO_ID
import BelgiUtil.ATTR_SEPERATOR
import BelgiUtil.ATTR_VALUE_EMPTY_CONSTRAINT
import BelgiUtil.SERIALIZING_SEPARATOR
import BelgiUtil.TAG_CONSTRAINT_END
import BelgiUtil.TAG_CONSTRAINT_START
import BelgiUtil.TAG_BELGI_END
import BelgiUtil.TAG_BELGI_START
import BelgiUtil.TAG_MARKUP_END
import BelgiUtil.TAG_MARKUP_START
import BelgiUtil.TAG_NAME_BELGI
import BelgiUtil.TAG_NAME_CONSTRAINT
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
    const val TAG_BELGI_START = "<$TAG_NAME_BELGI"
    const val TAG_BELGI_END = "/>"
    const val TAG_NAME_CONSTRAINT = "Constraint"
    const val TAG_CONSTRAINT_START = "<$TAG_NAME_CONSTRAINT"
    const val TAG_CONSTRAINT_END = "/>"

    const val ATTR_SEPERATOR = " "
    const val ATTR_EQUALS = "="
    const val ATTR_LABEL_ID = "id"
    const val ATTR_LABEL_CONSTRAINT_TO_ID = "to_id"
    const val ATTR_VALUE_EMPTY_CONSTRAINT = "-1"

    const val SERIALIZING_SEPARATOR = ":"

    val createId: Int get() = Random().nextInt().absoluteValue + 1

    fun buildXmlDocument(markup: String): Document {
        val inputSource = InputSource(StringReader(markup.wellFormedXml))
        val xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputSource)
        return xml
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

    override fun toString() = """
        $<$TAG_NAME_BELGI $ATTR_LABEL_ID="$id" />
    """.trimIndent()
}

enum class Edge(val serializedValue: Int) {
    START(0),
    TOP(1),
    RIGHT(2),
    BOTTOM(3);

    val v: Int get() = serializedValue
}

open class SubjectiveConstraint(val fromEdge: Edge,
                                val to: Pair<Belgi, Edge>,
                                val margin: Int = 0)

open class Constraint(val from: Pair<Int, Int>,
                      val to: Pair<Int, Int>,
                      val margin: Int = 0) {

    private val Pair<Int, Int>.id: Int get() = first
    private val Pair<Int, Int>.edge: Int get() = second

    private val Int.orEmpty: String get() = if (this > 0) this.toString() else ""

    override fun toString() =
            TAG_CONSTRAINT_START +
            from.id +
            SERIALIZING_SEPARATOR +
            from.edge +
            SERIALIZING_SEPARATOR +
            to.id +
            SERIALIZING_SEPARATOR +
            to.edge +
            SERIALIZING_SEPARATOR +
            margin.orEmpty +
            TAG_CONSTRAINT_END
}

val empty: Any get() = object : Any() {
    override fun toString() = """
        <$TAG_NAME_CONSTRAINT $ATTR_LABEL_CONSTRAINT_TO_ID=$ATTR_VALUE_EMPTY_CONSTRAINT />
    """.trimIndent() // <Constraint to_id="-1" />
}

operator fun List<Any>.get(edge: Edge) = this[edge.serializedValue]
operator fun MutableList<Any>.set(edge: Edge, a: Any) {
    this[edge.serializedValue] = a
}