import BelgiUtil.KEY_CONSTRAINT_TARGET_POLE
import BelgiUtil.KEY_CONSTRAINT_TARGET_ID
import BelgiUtil.KEY_MARGIN
import BelgiUtil.VALUE_EMPTY_CONSTRAINT
import BelgiUtil.KEY_OF_TYPE
import BelgiUtil.VALUE_TYPE_QATNAS
import Qatnas.Companion.qatnasMarker

open class Qatnas(val targetId: Int, val targetPole: Boolean) {

    constructor(targetId: Int, targetPole: Boolean, margin: Int) : this(targetId, targetPole) {
        this.margin = margin
    }

    var margin: Int = 0

    private val attrTargetId: String get() = "$KEY_CONSTRAINT_TARGET_ID : \"$targetId\""
    private val attrTargetPole: String get() = "$KEY_CONSTRAINT_TARGET_POLE : \"$targetPole\""
    private val attrMargin: String get() ="$KEY_MARGIN : \"$margin\""

    override fun toString() = """
        { $qatnasMarker, $attrTargetId, $attrTargetPole, $attrMargin },
    """.trimIndent()

    companion object {
        const val qatnasMarker = "$KEY_OF_TYPE : \"$VALUE_TYPE_QATNAS\""
    }
}

data class QatnasById(val fromEdge: Int,
                      val toBelgiId: Int,
                      val toEdge: Int,
                      val margin: Int)

open class SubjectiveQatnas(val fromEdge: Int,
                            val to: Pair<Belgi, Int>,
                            val margin: Int = 0) {
    constructor(fromEdge: Int, toBelgi: Belgi, toEdge: Int, margin: Int = 0) : this(fromEdge, toBelgi to toEdge, margin)


}

val empty: Any get() = object : Any() {
    override fun toString() = """
        { $qatnasMarker, $KEY_CONSTRAINT_TARGET_ID : $VALUE_EMPTY_CONSTRAINT },
    """.trimIndent()
}