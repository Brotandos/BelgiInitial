import org.json.JSONArray
import org.json.JSONObject

inline fun JSONArray.forEachObject(action: (index: Int, JSONObject) -> Unit) {
    for (i in 0 until length()) action(i, this.getJSONObject(i))
}