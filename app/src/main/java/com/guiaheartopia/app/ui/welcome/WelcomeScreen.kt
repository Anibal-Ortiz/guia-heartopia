package com.guiaheartopia.app.ui.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.guiaheartopia.app.domain.Countries
import com.guiaheartopia.app.domain.Country
import com.guiaheartopia.app.domain.CountryZone
import com.guiaheartopia.app.domain.PlayerProfile
import com.guiaheartopia.app.ui.components.DraftSearchField
import com.guiaheartopia.app.ui.components.PixelButton
import com.guiaheartopia.app.ui.components.PixelCard
import com.guiaheartopia.app.ui.components.StepBadge
import com.guiaheartopia.app.ui.theme.Cream
import com.guiaheartopia.app.ui.theme.Green
import com.guiaheartopia.app.ui.theme.Ink
import com.guiaheartopia.app.ui.theme.Line
import com.guiaheartopia.app.ui.theme.Mint
import com.guiaheartopia.app.ui.theme.Muted
import com.guiaheartopia.app.ui.theme.Paper
import java.text.Normalizer

/** Países que se muestran primero cuando el buscador está vacío. */
private val featuredCountryIds = listOf("CL", "AR", "MX", "ES", "CO", "PE")

/** Minúsculas sin diacríticos: "México" -> "mexico", "Córdoba" -> "cordoba". */
private fun plainText(value: String): String =
    Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        .lowercase()

private fun orderedCountries(query: String): List<Country> {
    val q = plainText(query.trim())
    val matches = Countries.all.filter { q.isEmpty() || plainText(it.name).contains(q) }
    return if (q.isEmpty()) {
        val featured = featuredCountryIds.mapNotNull { id -> matches.firstOrNull { it.id == id } }
        val rest = matches.filterNot { it.id in featuredCountryIds }.sortedBy { plainText(it.name) }
        featured + rest
    } else {
        matches.sortedBy { plainText(it.name) }
    }
}

private fun filteredZones(country: Country, query: String): List<CountryZone> {
    val q = plainText(query.trim())
    return country.zones.filter {
        q.isEmpty() || plainText(it.label).contains(q) || plainText(it.zoneId).contains(q)
    }
}

@Composable
fun WelcomeScreen(
    initial: PlayerProfile,
    onContinue: (PlayerProfile) -> Unit,
    onPrivacy: () -> Unit,
    onTerms: () -> Unit,
) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var name by rememberSaveable { mutableStateOf(initial.name) }
    var countryId by rememberSaveable { mutableStateOf(initial.countryId) }
    var zoneId by rememberSaveable { mutableStateOf(initial.zoneId) }
    var regionKey by rememberSaveable { mutableStateOf(initial.regionKey) }
    val country = Countries.byId(countryId)
    // Antes se escribía estado dentro de la composición. Ahora se deriva y se corrige en un efecto.
    val zoneValid by remember(country, regionKey, zoneId) {
        derivedStateOf { country.zones.any { it.key == regionKey || it.zoneId == zoneId } }
    }
    val effectiveZoneId = if (zoneValid) zoneId else country.zones.first().zoneId
    val effectiveRegionKey = if (zoneValid) regionKey else country.zones.first().key
    LaunchedEffect(country, zoneValid) {
        if (!zoneValid) {
            val first = country.zones.first()
            zoneId = first.zoneId
            regionKey = first.key
        }
    }
    val total = 4
    val ready = step != 0 || name.isNotBlank()

    Column(Modifier.fillMaxSize()) {
        // Cabecera fija: no se desplaza con la lista.
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StepBadge()
            Column(Modifier.weight(1f)) {
                Text("WikiFan para Heartopia", style = MaterialTheme.typography.titleSmall)
                Text("Paso ${step + 1} de $total", style = MaterialTheme.typography.bodySmall)
            }
        }

        val contentModifier = Modifier
            .weight(1f)
            .padding(horizontal = 20.dp)

        when (step) {
            0 -> Column(contentModifier.verticalScroll(rememberScrollState())) {
                StepName(name, { name = it }, onPrivacy, onTerms)
                if (name.isBlank()) {
                    Text(
                        "Escribe un nombre para continuar.",
                        color = Muted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            1 -> StepCountry(contentModifier, countryId) { id ->
                countryId = id
                val first = Countries.byId(id).zones.first()
                zoneId = first.zoneId
                regionKey = first.key
            }
            2 -> StepZone(contentModifier, country, regionKey) { zone ->
                regionKey = zone.key
                zoneId = zone.zoneId
            }
            else -> Column(contentModifier.verticalScroll(rememberScrollState())) {
                StepSummary(
                    name,
                    country.name,
                    Countries.label(country.id, effectiveZoneId, effectiveRegionKey),
                )
            }
        }

        // Barra inferior fija: siempre visible, por encima del teclado y de la barra del sistema.
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Paper,
            shadowElevation = 8.dp,
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (step > 0) {
                    TextButton(onClick = { step -= 1 }) { Text("← Anterior", color = Green) }
                }
                Spacer(Modifier.weight(1f))
                PixelButton(
                    text = if (step == total - 1) "Entrar →" else "Continuar →",
                    enabled = ready,
                    onClick = {
                        if (!ready) return@PixelButton
                        if (step < total - 1) {
                            step += 1
                        } else {
                            onContinue(
                                initial.copy(
                                    name = name.trim(),
                                    countryId = countryId,
                                    zoneId = zoneId,
                                    regionKey = regionKey,
                                    onboarded = true,
                                ),
                            )
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun StepName(
    name: String,
    onName: (String) -> Unit,
    onPrivacy: () -> Unit,
    onTerms: () -> Unit,
) {
    Text("¿Cómo te llamamos?", style = MaterialTheme.typography.headlineSmall)
    Text(
        "Lo que anotes se queda en el teléfono.",
        style = MaterialTheme.typography.bodyLarge,
    )
    OutlinedTextField(
        name,
        onName,
        label = { Text("Tu nombre") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    )
    TextButton(onClick = onPrivacy) { Text("Política de privacidad") }
    TextButton(onClick = onTerms) { Text("Términos") }
}

@Composable
private fun SearchField(
    query: TextFieldValue,
    onQuery: (TextFieldValue) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    // Buscador compartido: mismo comportamiento que el de Cocina, con botón de limpiar.
    DraftSearchField(
        value = query,
        onValueChange = onQuery,
        placeholder = placeholder,
        modifier = modifier,
        showClear = true,
    )
}

@Composable
private fun StepCountry(
    modifier: Modifier = Modifier,
    selected: String,
    onPick: (String) -> Unit,
) {
    // Estado síncrono: el TextField no espera a ningún Flow antes de actualizar su value.
    var query by remember { mutableStateOf(TextFieldValue("")) }
    val focusManager = LocalFocusManager.current
    val items = remember(query.text) { orderedCountries(query.text) }
    val selectedName = Countries.byId(selected).name

    Column(modifier) {
        Text("Tu país", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Sirve para el calendario. No usamos la ubicación del teléfono: solo el país que elijas.",
            style = MaterialTheme.typography.bodyMedium,
        )
        SearchField(query, { query = it }, "Buscar país", Modifier.padding(top = 12.dp))

        // Resumen compacto de la selección actual, justo debajo del buscador.
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Mint)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("✓", color = Green, style = MaterialTheme.typography.labelMedium)
            Text(
                "País elegido: $selectedName",
                style = MaterialTheme.typography.labelMedium,
                color = Green,
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            items(items, key = { it.id }) { country ->
                OptionRow(
                    label = country.name,
                    secondary = null,
                    selected = country.id == selected,
                ) {
                    onPick(country.id)
                    focusManager.clearFocus()
                }
            }
        }
    }
}

@Composable
private fun StepZone(
    modifier: Modifier = Modifier,
    country: Country,
    selectedKey: String,
    onPick: (CountryZone) -> Unit,
) {
    var query by remember { mutableStateOf(TextFieldValue("")) }
    val focusManager = LocalFocusManager.current
    val items = remember(country, query.text) { filteredZones(country, query.text) }

    Column(modifier) {
        Text(
            if (country.zones.size > 1) "Estado o región" else "Zona horaria",
            style = MaterialTheme.typography.headlineSmall,
        )
        PixelCard(container = Cream) {
            Text(
                "Las horas del juego se muestran en la zona que elijas. " +
                    "Puedes buscar por región o por zona horaria.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        SearchField(query, { query = it }, "Buscar región o zona", Modifier.padding(top = 12.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            items(items, key = { it.key }) { zone ->
                OptionRow(
                    label = zone.label,
                    secondary = zone.zoneId,
                    selected = zone.key == selectedKey,
                ) {
                    // Un toque fija región y zona a la vez (regionKey codifica ambos).
                    onPick(zone)
                    focusManager.clearFocus()
                }
            }
        }
    }
}

@Composable
private fun OptionRow(
    label: String,
    secondary: String?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) Mint else Paper)
            .border(1.dp, if (selected) Green else Line, shape)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected) Green else Ink,
            )
            if (secondary != null) {
                Text(secondary, style = MaterialTheme.typography.bodySmall, color = Muted)
            }
        }
        if (selected) {
            Text("✓", color = Green, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun StepSummary(name: String, country: String, zone: String) {
    Text("Listo, $name", style = MaterialTheme.typography.headlineSmall)
    PixelCard {
        Text(country, style = MaterialTheme.typography.titleMedium)
        Text(zone, style = MaterialTheme.typography.bodyMedium)
    }
    Text("Puedes cambiarlo cuando quieras en Ajustes.", style = MaterialTheme.typography.bodyMedium)
}
