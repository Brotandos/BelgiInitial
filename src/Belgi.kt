import BelgiUtil.ATTR_LABEL_ID
import BelgiUtil.JSON_KEY_OF_TYPE
import BelgiUtil.JSON_TYPE_BELGI
import BelgiUtil.POLE_MINUS
import BelgiUtil.POLE_PLUS
import BelgiUtil.createId
import java.util.*
import kotlin.math.absoluteValue

object BelgiUtil {
    const val JSON_KEY_OF_TYPE = "type"
    const val JSON_TYPE_BELGI = 0
    const val JSON_TYPE_QATNAS = 1

    const val POLE_MINUS = false
    const val POLE_PLUS = true

    const val ATTR_LABEL_ID = "id"
    const val ATTR_LABEL_CONSTRAINT_TO_ID = "toId"
    const val ATTR_LABEL_CONSTRAINT_TO_EDGE = "toPole"
    const val ATTR_LABEL_MARGIN = "margin"
    const val ATTR_VALUE_EMPTY_CONSTRAINT = -1

    val createId: Int get() = Random().nextInt().absoluteValue + 1

    fun wrapToBelgiNorm(markup: String): List<String> =
            markup
                    .replace("*", empty.toString())
                    .split("\n")
                    .map { "[$it]" }
}

open class Belgi(val id: Int) {

    val constraints = mutableListOf<SubjectiveConstraint>()

    constructor() : this(createId)

    private val belgiMarker = "$JSON_KEY_OF_TYPE : \"$JSON_TYPE_BELGI\""

    private val attrId = "$ATTR_LABEL_ID : \"$id\""

    override fun toString() = """
        { $belgiMarker, $attrId },
    """.trimIndent()

    operator fun minus(margin: Int): Qatnas =
            Qatnas(id, POLE_MINUS, margin)

    operator fun plus(margin: Int): Qatnas =
            Qatnas(id, POLE_PLUS, margin)
}

open class SubjectiveConstraint(val fromPole: Boolean,
                                val to: Pair<Belgi, Boolean>,
                                val margin: Int = 0) {

    constructor(fromPole: Boolean, toBelgi: Belgi, toPole: Boolean, margin: Int = 0) : this(fromPole, toBelgi to toPole, margin)
}