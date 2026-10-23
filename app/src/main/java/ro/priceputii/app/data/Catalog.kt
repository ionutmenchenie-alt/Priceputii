package ro.priceputii.app.data

data class TaskCategory(val name: String, val tasks: List<String>)

/** Fixed catalog shared by Priceput skills and posted jobs, so the two can be matched. */
object Catalog {
    const val CITY = "Târgu Jiu"
    const val TRANSPORT_CATEGORY = "Transport marfă"
    const val OTHER_TASK = "Altă lucrare"

    val categories = listOf(
        TaskCategory(
            "Grădină și curte",
            listOf(
                "tuns iarba", "cosit cu coasa sau motocoasa", "tăiat crengi", "toaletat pomi",
                "tuns gard viu", "săpat grădină", "plivit buruieni", "strâns frunze", "udat grădina",
                "plantat pomi", "plantat flori și răsaduri", "curățat curtea", "defrișat mărăciniș",
            ),
        ),
        TaskCategory(
            "Pomi, vie și grădinărit",
            listOf(
                "tăiat pomi", "scăpat de cioate", "tăiat via", "legat via", "săpat pomi/vie",
                "stropit pomi", "stropit via", "cules fructe", "cules struguri", "ajutor la făcut vin",
                "lucru în grădină", "lucru în solar", "montat sau reparat solar", "pregătit pământul",
                "plantat răsaduri", "legat roșii", "stropit legume", "udat grădina sau solarul",
                "cules legume",
            ),
        ),
        TaskCategory(
            "Lemne și încălzire",
            listOf(
                "tăiat lemne cu drujba", "spart lemne", "stivuit lemne", "cărat lemne în șopron",
                "descărcat lemne", "curățat soba", "curățat coșul de fum",
                "montat sau curățat burlane de sobă", "curățat cenușa",
            ),
        ),
        TaskCategory(
            "Apă și instalații mici",
            listOf(
                "curățat fântâna", "desfundat țevi", "desfundat chiuvetă", "desfundat WC",
                "schimbat robinet", "schimbat garnituri", "reparat scurgeri mici",
                "montat furtun și robinet de grădină", "curățat jgheaburi și burlane",
            ),
        ),
        TaskCategory(
            "Electrice mărunțe",
            listOf(
                "schimbat becuri", "schimbat prize", "schimbat întrerupătoare", "montat lustră sau aplică",
                "montat lampă de exterior", "montat sonerie", "montat antenă sau televizor pe perete",
            ),
        ),
        TaskCategory(
            "Acoperiș, gard, construcții mici",
            listOf(
                "reparat acoperiș", "schimbat țigle sparte", "reparat gard", "montat gard", "vopsit gard",
                "reparat poartă", "turnat beton mic (alee, stâlp)", "zidit mic", "tencuit mic",
                "reparat trepte", "montat jgheaburi",
            ),
        ),
        TaskCategory(
            "Casă și interior",
            listOf(
                "zugrăvit cameră", "vopsit uși și ferestre", "montat mobilă", "montat rafturi",
                "agățat tablouri și perdele", "reparat uși", "schimbat yală", "montat plasă de țânțari",
                "reparat mobilă", "curățenie generală", "spălat geamuri", "spălat covoare",
                "curățenie după renovare",
            ),
        ),
        TaskCategory(
            "Cărat și mutări",
            listOf(
                "cărat mobilă", "ajutor la mutare", "încărcat și descărcat", "golit podul sau pivnița",
                "dus moloz", "dus fier vechi", "cărat saci (cereale, cartofi, ciment)",
            ),
        ),
        TaskCategory(
            TRANSPORT_CATEGORY,
            listOf(
                "marfă mică cu remorcă sau dubă", "adus cumpărături mari",
                "transport mobilă și electrocasnice", "transport materiale de construcții",
                "transport lemne", "transport saci (cereale, cartofi)",
            ),
        ),
        TaskCategory(
            "Iarnă",
            listOf("dat zăpada", "spart gheața", "presărat sare sau nisip", "dat zăpada de pe acoperiș"),
        ),
        TaskCategory(
            "Animale și agricultură",
            listOf(
                "ajutor la tăiat porcul", "curățat grajd sau coșar", "construit sau reparat coțet",
                "hrănit animalele când pleci", "săpat sau prășit", "plantat cartofi", "cules cartofi",
                "cules porumb", "cosit fân", "strâns fân",
            ),
        ),
        TaskCategory(
            "Alte ajutoare",
            listOf(
                "schimbat roțile la mașină", "spălat mașina", "reparat bicicletă",
                "cumpărături pentru vârstnici", OTHER_TASK,
            ),
        ),
    )

    /** Units a Priceput can price a task by. */
    val units = listOf("lucrare", "oră", "mc", "m²", "bucată", "pom", "butuc", "km")

    fun categoryOf(task: String): String? = categories.firstOrNull { task in it.tasks }?.name

    /** Târgu Jiu neighborhoods followed by its component localities. */
    val zones = listOf(
        "Centru", "Obreja", "Meteor", "Luncilor", "Debarcader", "Preajba", "Livezeni", "Podmol",
        "9 Mai", "Vaporul", "Primăverii (Vărsături)", "Dumbrava", "Ieti", "Pandurașu", "Panduri Est",
        "Bârsești", "Drăgoieni", "Iezureni", "Polata", "Preajba Mare", "Romanești", "Slobozia", "Ursați",
    )
}
