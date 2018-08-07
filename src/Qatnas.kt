import BelgiUtil.ATTR_LABEL_CONSTRAINT_TO_EDGE
import BelgiUtil.ATTR_LABEL_CONSTRAINT_TO_ID
import BelgiUtil.ATTR_LABEL_MARGIN
import BelgiUtil.ATTR_VALUE_EMPTY_CONSTRAINT
import BelgiUtil.TAG_NAME_CONSTRAINT

open class Qatnas(val toId: Int, val toEdge: Edge) {

    constructor(toId: Int, toEdge: Edge, margin: Int) : this(toId, toEdge) {
        this.margin = margin
    }

    var margin: Int = 0

    infix fun by(margin: Int) = this.apply { this.margin = margin }

    private val toIdXmlAttr: String get() = "$ATTR_LABEL_CONSTRAINT_TO_ID=\"$toId\""
    private val toEdgeXmlAttr: String get() = "$ATTR_LABEL_CONSTRAINT_TO_EDGE=\"${toEdge.serializedValue}\""
    private val marginXmlAttr: String get() ="$ATTR_LABEL_MARGIN=\"$margin\""

    override fun toString() = """
        <$TAG_NAME_CONSTRAINT $toIdXmlAttr $toEdgeXmlAttr $marginXmlAttr></$TAG_NAME_CONSTRAINT>
    """.trimIndent()
}

// TODO throw exception if invalid constraint (e.g: start to top)
val Int.start: Qatnas get() = Qatnas(this, Edge.START)
val Int.top: Qatnas get() = Qatnas(this, Edge.TOP)
val Int.end: Qatnas get() = Qatnas(this, Edge.END)
val Int.bottom: Qatnas get() = Qatnas(this, Edge.BOTTOM)

val empty: Any get() = object : Any() {
    override fun toString() = """
        <$TAG_NAME_CONSTRAINT $ATTR_LABEL_CONSTRAINT_TO_ID="$ATTR_VALUE_EMPTY_CONSTRAINT" />
    """.trimIndent() // <Constraint to_id="-1" />
}