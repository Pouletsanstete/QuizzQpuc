// ============================================================================
// Sauvegarde des parties en pause (SharedPreferences) + boîte de dialogue « Quitter ? »
// ============================================================================

package com.example.quizzqpuc

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject

object Sauvegarde {
    private fun prefs(ctx: Context) =
        ctx.applicationContext.getSharedPreferences("parties", Context.MODE_PRIVATE)

    fun lire(ctx: Context, cle: String): String? = prefs(ctx).getString(cle, null)

    fun ecrire(ctx: Context, cle: String, valeur: String?) {
        val e = prefs(ctx).edit()
        if (valeur == null) e.remove(cle) else e.putString(cle, valeur)
        e.apply()
    }
}

data class PartieDepts(val questions: List<Question>, val index: Int, val reponses: List<Boolean>)
data class PartiePays(val questions: List<QuestionPays>, val index: Int, val reponses: List<Boolean>)

private fun versJson(reponses: List<Boolean>): JSONArray {
    val r = JSONArray()
    reponses.forEach { r.put(it) }
    return r
}

private fun versJsonTextes(l: List<String>): JSONArray {
    val a = JSONArray()
    l.forEach { a.put(it) }
    return a
}

// ---------------------------------------------------------------- départements

fun sauverPartieDepts(ctx: Context, questions: List<Question>, index: Int, reponses: List<Boolean>) {
    val q = JSONArray()
    questions.forEach { x ->
        q.put(
            JSONObject()
                .put("d", x.dept.numero)
                .put("c", x.carte)
                .put("m", versJsonTextes(x.champs.map { it.name }))
        )
    }
    val o = JSONObject().put("q", q).put("i", index).put("r", versJson(reponses))
    Sauvegarde.ecrire(ctx, "depts", o.toString())
}

fun effacerPartieDepts(ctx: Context) = Sauvegarde.ecrire(ctx, "depts", null)

fun lirePartieDepts(ctx: Context): PartieDepts? {
    try {
        val s = Sauvegarde.lire(ctx, "depts") ?: return null
        val o = JSONObject(s)
        val q = o.getJSONArray("q")
        val r = o.getJSONArray("r")
        val questions = List(q.length()) { i ->
            val e = q.getJSONObject(i)
            val m = e.getJSONArray("m")
            Question(
                DEPARTEMENTS.first { it.numero == e.getString("d") },
                e.getBoolean("c"),
                List(m.length()) { Mode.valueOf(m.getString(it)) }
            )
        }
        val index = o.getInt("i")
        if (questions.isEmpty() || index !in questions.indices) return null
        return PartieDepts(questions, index, List(r.length()) { r.getBoolean(it) })
    } catch (e: Exception) {
        return null
    }
}

// ---------------------------------------------------------------- pays

fun sauverPartiePays(ctx: Context, questions: List<QuestionPays>, index: Int, reponses: List<Boolean>) {
    val q = JSONArray()
    questions.forEach { x ->
        q.put(
            JSONObject()
                .put("p", x.pays.iso3)
                .put("t", x.type.name)
                .put("m", versJsonTextes(x.champs.map { it.name }))
                .put("c", versJsonTextes(x.choix.map { it.iso3 }))
        )
    }
    val o = JSONObject().put("q", q).put("i", index).put("r", versJson(reponses))
    Sauvegarde.ecrire(ctx, "pays", o.toString())
}

fun effacerPartiePays(ctx: Context) = Sauvegarde.ecrire(ctx, "pays", null)

fun lirePartiePays(ctx: Context, tous: List<Pays>): PartiePays? {
    try {
        val s = Sauvegarde.lire(ctx, "pays") ?: return null
        val o = JSONObject(s)
        val q = o.getJSONArray("q")
        val r = o.getJSONArray("r")
        val questions = List(q.length()) { i ->
            val e = q.getJSONObject(i)
            val m = e.getJSONArray("m")
            val c = e.getJSONArray("c")
            QuestionPays(
                tous.first { it.iso3 == e.getString("p") },
                TypeQ.valueOf(e.getString("t")),
                List(m.length()) { ModePays.valueOf(m.getString(it)) },
                List(c.length()) { k -> tous.first { it.iso3 == c.getString(k) } }
            )
        }
        val index = o.getInt("i")
        if (questions.isEmpty() || index !in questions.indices) return null
        return PartiePays(questions, index, List(r.length()) { r.getBoolean(it) })
    } catch (e: Exception) {
        return null
    }
}

// ---------------------------------------------------------------- interface

@Composable
fun DialogQuitter(onContinuer: () -> Unit, onPause: () -> Unit, onAbandonner: () -> Unit) {
    AlertDialog(
        onDismissRequest = onContinuer,
        title = { Text("Quitter la partie ?") },
        text = { Text("Tu peux la mettre en pause : elle sera sauvegardée et tu pourras la reprendre depuis l'écran des paramètres du quiz.") },
        confirmButton = {
            Column(horizontalAlignment = Alignment.End) {
                TextButton(onClick = onContinuer) { Text("Continuer la partie") }
                TextButton(onClick = onPause) { Text("Mettre en pause") }
                TextButton(onClick = onAbandonner) { Text("Abandonner") }
            }
        }
    )
}

@Composable
fun CarteReprise(texte: String, onReprendre: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Partie en pause", fontWeight = FontWeight.Bold)
            Text(texte)
            Spacer(Modifier.height(8.dp))
            Button(onClick = onReprendre) { Text("Reprendre la partie") }
        }
    }
}
