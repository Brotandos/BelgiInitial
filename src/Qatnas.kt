import BelgiUtil.ATTR_LABEL_CONSTRAINT_TO_EDGE
import BelgiUtil.ATTR_LABEL_CONSTRAINT_TO_ID
import BelgiUtil.ATTR_LABEL_MARGIN
import BelgiUtil.ATTR_VALUE_EMPTY_CONSTRAINT
import BelgiUtil.JSON_KEY_OF_TYPE
import BelgiUtil.JSON_TYPE_QATNAS
import Qatnas.Companion.qatnasMarker

open class Qatnas(val toId: Int, val toPole: Boolean) {

    constructor(toId: Int, toPole: Boolean, margin: Int) : this(toId, toPole) {
        this.margin = margin
    }

    var margin: Int = 0

    private val attrToId: String get() = "$ATTR_LABEL_CONSTRAINT_TO_ID : \"$toId\""
    private val attrToPole: String get() = "$ATTR_LABEL_CONSTRAINT_TO_EDGE : \"$toPole\""
    private val attrMargin: String get() ="$ATTR_LABEL_MARGIN : \"$margin\""

    override fun toString() = """
        { $qatnasMarker, $attrToId, $attrToPole, $attrMargin },
    """.trimIndent()

    companion object {
        const val qatnasMarker = "$JSON_KEY_OF_TYPE : $JSON_TYPE_QATNAS"
    }
}

val empty: Any get() = object : Any() {
    override fun toString() = """
        { $qatnasMarker, $ATTR_LABEL_CONSTRAINT_TO_ID : $ATTR_VALUE_EMPTY_CONSTRAINT },
    """.trimIndent()
}