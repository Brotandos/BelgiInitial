import BelgiUtil.KEY_BELGI_ID
import BelgiUtil.KEY_OF_TYPE
import BelgiUtil.VALUE_TYPE_BELGI
import BelgiUtil.POLE_MINUS
import BelgiUtil.POLE_PLUS
import BelgiUtil.createId
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.math.absoluteValue

object BelgiUtil {
    const val KEY_OF_TYPE = "type"
    const val VALUE_TYPE_HANDLED = -1
    const val VALUE_TYPE_BELGI = 0
    const val VALUE_TYPE_QATNAS = 1

    const val POLE_MINUS = false
    const val POLE_PLUS = true

    const val KEY_BELGI_ID = "id"
    const val KEY_CONSTRAINT_TARGET_ID = "targetId"
    const val KEY_CONSTRAINT_TARGET_POLE = "targetPole"
    const val KEY_MARGIN = "margin"

    const val VALUE_EMPTY_CONSTRAINT = -1
    const val VALUE_START_EDGE = 0
    const val VALUE_TOP_EDGE = 1
    const val VALUE_END_EDGE = 2
    const val VALUE_BOTTOM_EDGE = 3

    val createId: Int get() = Random().nextInt().absoluteValue + 1

    fun wrapToBelgiNorm(markup: String): List<JSONArray> =
            markup
                    .replace("*", empty.toString())
                    .split("\n")
                    .map { JSONArray("[$it]") }

    /**
     * @param   jsonArrays  markup's each line list, wrapped into JSONArray
     * @return              list of pair, first is belgi's json representation, second is its constraints' json representation
     * @see                 Belgi
     * @see                 QatnasById
     * */
    fun getBelgiToConstraintsList(jsonArrays: List<JSONArray>): List<Pair<Belgi, List<QatnasById>>> {
        val belgiToConstraintsPairList = mutableListOf<Pair<Belgi, List<QatnasById>>>()

        jsonArrays.forEachIndexed { arrayIndex, jsonArray ->
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                if (jsonObject.isBelgi) {
                    val constraints = getHorizontalConstraints(jsonArray, i)

                    if (arrayIndex == 0) RuntimeException("There must be constraint on top of belgi")
                    val topJsonArray = jsonArrays[arrayIndex - 1]
                    if (arrayIndex == jsonArrays.size - 1) RuntimeException("There must be constraint on bottom of belgi")
                    val bottomJsonArray = jsonArrays[arrayIndex + 1]
                    constraints += getVerticalConstraints(topJsonArray, bottomJsonArray, belgiToConstraintsPairList.size)

                    belgiToConstraintsPairList += Belgi(jsonObject.getInt(KEY_BELGI_ID)) to constraints
                }
            }
        }

        return belgiToConstraintsPairList
    }

    /**
     * @param   jsonArray   array of json of markup's exact line
     * @param   index       index of current belgi
     * @return              list of horizontal constraints' json representation
     * */
    private fun getHorizontalConstraints(jsonArray: JSONArray, index: Int): MutableList<QatnasById> {
        val horizontalConstraints = mutableListOf<QatnasById>()
        if (index == 0 || index == jsonArray.length() - 1) throw RuntimeException("There must be constraints on both side of belgi!")

        val leftIndex = index - 1
        getConstraint(jsonArray.getJSONObject(leftIndex), "left", VALUE_START_EDGE)
                .let { if (it != null) horizontalConstraints += it }

        val rightIndex = index + 1
        getConstraint(jsonArray.getJSONObject(rightIndex), "right", VALUE_END_EDGE)
                .let { if (it != null) horizontalConstraints += it}

        jsonArray.getJSONObject(leftIndex).put(KEY_OF_TYPE, VALUE_TYPE_HANDLED)
        jsonArray.getJSONObject(rightIndex).put(KEY_OF_TYPE, VALUE_TYPE_HANDLED)

        return horizontalConstraints
    }

    /**
     * @param   above       objects' json representation array, that are above of current belgi's line
     * @param   below       objects' json representation array, that are below of current belgi's line
     * @param   objectIndex current belgi's index inside pair <belgi, constraints>
     * @return              list of vertical constraints' json representation
     * */
    private fun getVerticalConstraints(above: JSONArray, below: JSONArray, objectIndex: Int): List<QatnasById> {
        val verticalConstraints = mutableListOf<QatnasById>()

        mutableListOf<Pair<Int, JSONObject>>().let { constraintsAbove ->
            above.forEachObject { index, jsonObject ->
                if (jsonObject.getInt(KEY_OF_TYPE) == VALUE_TYPE_QATNAS)
                    constraintsAbove += index to jsonObject
            }
            getConstraint(constraintsAbove[objectIndex].second, "above", VALUE_TOP_EDGE, false)
                    .let { if (it != null) verticalConstraints += it }
            above.getJSONObject(constraintsAbove[objectIndex].first).put(KEY_OF_TYPE, VALUE_TYPE_HANDLED)
        }

        mutableListOf<Pair<Int, JSONObject>>().let { constraintsBelow ->
            below.forEachObject { index, jsonObject ->
                if (jsonObject.getInt(KEY_OF_TYPE) == VALUE_TYPE_QATNAS)
                    constraintsBelow += index to jsonObject
            }
            getConstraint(constraintsBelow[objectIndex].second, "below", VALUE_BOTTOM_EDGE, false)
                    .let { if (it != null) verticalConstraints += it }
            below.getJSONObject(constraintsBelow[objectIndex].first).put(KEY_OF_TYPE, VALUE_TYPE_HANDLED)
        }

        return verticalConstraints
    }

    /**
     * @param   jsonConstraint  json object of constraint
     * @param   sideLabel       label of side, it's need for exception
     * @param   fromEdge        edge, where starts constraint
     * @param   isHorizontal    if constraint is horizontal marker
     * @return                  constraint by id, or null, if it's empty constraint
     * @see                     empty
     * */
    private fun getConstraint(jsonConstraint: JSONObject, sideLabel: String, fromEdge: Int, isHorizontal: Boolean = true): QatnasById? {
        if (jsonConstraint.getInt(KEY_OF_TYPE) != VALUE_TYPE_QATNAS) RuntimeException("There must be constraint on the $sideLabel side of belgi")
        if (jsonConstraint.getInt(KEY_CONSTRAINT_TARGET_ID) == VALUE_EMPTY_CONSTRAINT) return null
        return QatnasById (
                fromEdge,
                jsonConstraint.getInt(KEY_CONSTRAINT_TARGET_ID),
                getEdge(isHorizontal, jsonConstraint.getBoolean(KEY_CONSTRAINT_TARGET_POLE)),
                jsonConstraint.getInt(KEY_MARGIN)
        )
    }

    /**
     * @param   isHorizontal    if constraint must be horizontal
     * @param   isPositivePole  if constraint edge is in positive side (top or end) by x to y axis
     * @return                  edge code
     * @see                     SubjectiveQatnas.fromEdge
     * @see                     QatnasById.fromEdge
     * @see                     QatnasById.toEdge
     * @see                     VALUE_START_EDGE
     * @see                     VALUE_TOP_EDGE
     * @see                     VALUE_END_EDGE
     * @see                     VALUE_BOTTOM_EDGE
     * */
    private fun getEdge(isHorizontal: Boolean, isPositivePole: Boolean): Int =
            if (isHorizontal)
                if (isPositivePole) VALUE_END_EDGE else VALUE_START_EDGE
            else
                if (isPositivePole) VALUE_TOP_EDGE else VALUE_BOTTOM_EDGE

    private val JSONObject.isBelgi: Boolean get() = getInt(KEY_OF_TYPE) == VALUE_TYPE_BELGI
}

open class Belgi(val id: Int) {

    val qatnasList = mutableListOf<SubjectiveQatnas>()

    constructor() : this(createId)

    private val belgiMarker = "$KEY_OF_TYPE : \"$VALUE_TYPE_BELGI\""

    private val attrId = "$KEY_BELGI_ID : \"$id\""

    override fun toString() = """
        { $belgiMarker, $attrId },
    """.trimIndent()

    operator fun minus(margin: Int): Qatnas =
            Qatnas(id, POLE_MINUS, margin)

    operator fun plus(margin: Int): Qatnas =
            Qatnas(id, POLE_PLUS, margin)
}
