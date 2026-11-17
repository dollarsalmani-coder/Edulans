package com.example.data.ai

import com.example.model.EducationalFact
import com.example.model.ObjectAnalysisResult
import com.example.model.PracticalSolution
import com.example.model.QuickQuizQuestion
import com.example.model.StructurePart
import com.example.model.StudyLanguage
import kotlinx.coroutines.delay
import java.util.UUID

/**
 * Placeholder sample implementation used when Firebase AI Logic is not enabled.
 * All outputs are clearly marked with isSamplePlaceholder = true.
 */
class SampleObjectAnalysisRepository : ObjectAnalysisRepository {

    override suspend fun analyzeObject(
        queryText: String,
        compressedImagePath: String?,
        payloadKb: Int,
        targetLanguage: StudyLanguage,
        lowBandwidthMode: Boolean
    ): ObjectAnalysisResult {
        delay(if (lowBandwidthMode) 100L else 180L)

        // Construct the language-aware analysis prompt parameter
        val promptSpec = ObjectAnalysisRepository.buildAnalysisPrompt(
            queryText = queryText,
            targetLanguage = targetLanguage,
            lowBandwidthMode = lowBandwidthMode
        )

        val normalized = queryText.trim().lowercase()
        val effectivePayloadKb = when {
            payloadKb > 0 -> payloadKb
            lowBandwidthMode -> 18
            else -> 64
        }

        val base = when {
            normalized.containsAny("leaf", "plant", "tree", "flower", "botan", "chlorophyll", "hoja", "feuille", "پت") ->
                createBotanicalLeafSample(imageSource = compressedImagePath, payloadKb = effectivePayloadKb)

            normalized.containsAny("watch", "clock", "gear", "cog", "time", "mechanic", "reloj", "montre") ->
                createMechanicalWatchSample(imageSource = compressedImagePath, payloadKb = effectivePayloadKb)

            normalized.containsAny("solar", "panel", "photovoltaic", "sun", "energy", "silicon", "cell") ->
                createSolarPanelSample(imageSource = compressedImagePath, payloadKb = effectivePayloadKb)

            normalized.containsAny("filter", "water", "purif", "ceramic", "clay") ->
                createWaterFilterSample(imageSource = compressedImagePath, payloadKb = effectivePayloadKb)

            normalized.containsAny("motor", "circuit", "battery", "chip", "robot", "magnet", "fan") ->
                createElectricMotorSample(imageSource = compressedImagePath, payloadKb = effectivePayloadKb)

            normalized.isNotBlank() ->
                createCustomSearchSample(
                    queryTitle = queryText.trim().replaceFirstChar { it.uppercase() },
                    imageSource = compressedImagePath,
                    payloadKb = effectivePayloadKb
                )

            compressedImagePath != null ->
                createPhotographedSample(
                    imageSource = compressedImagePath,
                    payloadKb = effectivePayloadKb
                )

            else ->
                createBotanicalLeafSample(imageSource = "drawable://img_sample_leaf", payloadKb = effectivePayloadKb)
        }

        return localizeSampleResult(base, targetLanguage)
    }

    override suspend fun translateAnalysis(
        existing: ObjectAnalysisResult,
        targetLanguage: StudyLanguage
    ): ObjectAnalysisResult {
        delay(220L)
        return localizeSampleResult(existing, targetLanguage)
    }

    fun getStarterOfflineGuides(): List<ObjectAnalysisResult> = listOf(
        createBotanicalLeafSample(
            id = "starter_leaf",
            imageSource = "drawable://img_sample_leaf",
            payloadKb = 24
        ).copy(isSavedOffline = true),
        createMechanicalWatchSample(
            id = "starter_watch",
            imageSource = "drawable://img_sample_watch",
            payloadKb = 28
        ).copy(isSavedOffline = true),
        createSolarPanelSample(
            id = "starter_solar",
            imageSource = "drawable://img_sample_solar",
            payloadKb = 22
        ).copy(isSavedOffline = true)
    )

    private fun createBotanicalLeafSample(
        id: String = "study_${UUID.randomUUID().toString().take(8)}",
        imageSource: String?,
        payloadKb: Int
    ): ObjectAnalysisResult = ObjectAnalysisResult(
        id = id,
        objectTitle = "Dicot Botanical Leaf",
        scientificOrTechnicalName = "Folia angiospermae · Vascular Photosynthetic Organ",
        category = "Biology",
        summary = "Sample Guide: A vascular plant leaf acts as a natural solar reactor. Its layered tissues capture sunlight, exchange carbon dioxide and oxygen through microscopic pores, and transport sugars to the rest of the plant.",
        languageCode = "en",
        structures = listOf(
            StructurePart(
                layerOrder = 1,
                name = "Waxy Cuticle & Upper Epidermis",
                materialOrRole = "Lipid wax layer over protective dermal cells",
                functionDescription = "Seals moisture inside the leaf while staying transparent so sunlight reaches deeper photosynthetic cells."
            ),
            StructurePart(
                layerOrder = 2,
                name = "Palisade & Spongy Mesophyll",
                materialOrRole = "Chloroplast-dense parenchyma tissue",
                functionDescription = "Palisade cells absorb peak light energy; spongy cells create air spaces for rapid CO2 and O2 diffusion."
            ),
            StructurePart(
                layerOrder = 3,
                name = "Midrib & Vascular Bundles",
                materialOrRole = "Xylem and Phloem conduits",
                functionDescription = "Xylem delivers water and dissolved minerals upward from roots; phloem exports synthesized glucose."
            ),
            StructurePart(
                layerOrder = 4,
                name = "Stomata & Guard Cells",
                materialOrRole = "Turgor-controlled microscopic valves",
                functionDescription = "Open in daylight to draw in CO2 and close during drought or heat stress to prevent dehydration."
            )
        ),
        facts = listOf(
            EducationalFact(
                discipline = "Biochemistry",
                title = "Why Leaves Look Green",
                detail = "Chlorophyll-a and chlorophyll-b strongly absorb blue (~430 nm) and red (~662 nm) wavelengths while reflecting green light back to your eyes."
            ),
            EducationalFact(
                discipline = "Physics & Transport",
                title = "Solar-Powered Water Lift",
                detail = "Transpiration through stomata creates negative water tension that pulls water more than 100 meters up tall trees without a mechanical pump."
            ),
            EducationalFact(
                discipline = "Ecology",
                title = "Atmospheric Carbon Engine",
                detail = "A single mature broadleaf tree can absorb roughly 20–22 kg of carbon dioxide annually through its leaf canopy."
            )
        ),
        solutions = listOf(
            PracticalSolution(
                problemTitle = "Yellowing Leaves (Chlorosis)",
                solutionSteps = "Check topsoil moisture first, then inspect for nitrogen, magnesium, or iron deficiency. Improve drainage and feed balanced compost or chelated micronutrients.",
                careOrSustainabilityTip = "Water early in the morning at root level to minimize fungal spore spread and evaporation loss."
            ),
            PracticalSolution(
                problemTitle = "Wilting or Curling Edges",
                solutionSteps = "Move potted plants away from harsh midday glare, gently mist ambient air if dry, and check roots for compaction.",
                careOrSustainabilityTip = "Fallen leaves make nutrient-rich leaf mold mulch that retains soil moisture by up to 35%."
            )
        ),
        quiz = QuickQuizQuestion(
            question = "Which leaf structure regulates gas exchange and prevents excess water loss?",
            options = listOf(
                "Stomata with guard cells",
                "Xylem vessel walls",
                "Waxy upper cuticle alone",
                "Root cap hairs"
            ),
            correctIndex = 0,
            explanation = "Guard cells swell or shrink around each stoma pore to balance CO2 intake against water vapor loss."
        ),
        imageSource = imageSource ?: "drawable://img_sample_leaf",
        estimatedPayloadKb = payloadKb,
        isSamplePlaceholder = true
    )

    private fun createMechanicalWatchSample(
        id: String = "study_${UUID.randomUUID().toString().take(8)}",
        imageSource: String?,
        payloadKb: Int
    ): ObjectAnalysisResult = ObjectAnalysisResult(
        id = id,
        objectTitle = "Mechanical Watch Movement",
        scientificOrTechnicalName = "Caliber Escapement & Gear Train Mechanism",
        category = "Engineering",
        summary = "Sample Guide: A mechanical watch measures time without batteries by storing elastic energy in a coiled mainspring and releasing it in precise, rhythmic beats via an escapement and balance wheel.",
        languageCode = "en",
        structures = listOf(
            StructurePart(
                layerOrder = 1,
                name = "Mainspring & Barrel",
                materialOrRole = "High-elasticity cobalt-nickel alloy ribbon",
                functionDescription = "Stores mechanical potential energy when wound by the crown or automatic rotor."
            ),
            StructurePart(
                layerOrder = 2,
                name = "Going Gear Train",
                materialOrRole = "Brass toothed wheels & steel pinions",
                functionDescription = "Multiplies rotational speed step-by-step from the barrel to drive hour, minute, and second hands."
            ),
            StructurePart(
                layerOrder = 3,
                name = "Lever Escapement",
                materialOrRole = "Hardened steel escape wheel & synthetic ruby pallets",
                functionDescription = "Locks and releases the gear train in tiny discrete impulses—producing the classic ticking sound."
            ),
            StructurePart(
                layerOrder = 4,
                name = "Balance Wheel & Hairspring",
                materialOrRole = "Oscillating flywheel with microscopic spiral spring",
                functionDescription = "Oscillates at a constant natural frequency (typically 28,800 vibrations/hour) to govern accuracy."
            )
        ),
        facts = listOf(
            EducationalFact(
                discipline = "Materials Science",
                title = "Why Watches Use Ruby Jewels",
                detail = "Synthetic corundum (ruby) has a Mohs hardness of 9 and ultra-low friction against polished steel pivots, preventing wear over decades."
            ),
            EducationalFact(
                discipline = "Mechanics",
                title = "Energy Conservation in Miniature",
                detail = "A typical mainspring stores only ~0.0001 watt-hours—enough to run 130+ interlocking parts continuously for 40 to 72 hours."
            )
        ),
        solutions = listOf(
            PracticalSolution(
                problemTitle = "Watch Running Fast Suddenly",
                solutionSteps = "The hairspring coils may have become magnetized by speakers or phone magnets, shortening its effective swing. Use a simple watch demagnetizer.",
                careOrSustainabilityTip = "Avoid setting the calendar date between 9 PM and 3 AM when gear teeth are engaged."
            ),
            PracticalSolution(
                problemTitle = "Condensation Under the Crystal",
                solutionSteps = "Stop wearing the watch immediately, pull the crown out, and have a technician inspect the crown gaskets and dry the movement.",
                careOrSustainabilityTip = "Mechanical watches can run for generations with periodic lubrication—making them zero-e-waste instruments."
            )
        ),
        quiz = QuickQuizQuestion(
            question = "Why are synthetic ruby jewels placed at gear pivot bearings?",
            options = listOf(
                "To make the movement heavier",
                "To minimize friction and pivot wear",
                "To generate electricity",
                "To absorb magnetic fields"
            ),
            correctIndex = 1,
            explanation = "Corundum jewels are nearly as hard as diamond and dramatically reduce friction at microscopic gear axles."
        ),
        imageSource = imageSource ?: "drawable://img_sample_watch",
        estimatedPayloadKb = payloadKb,
        isSamplePlaceholder = true
    )

    private fun createSolarPanelSample(
        id: String = "study_${UUID.randomUUID().toString().take(8)}",
        imageSource: String?,
        payloadKb: Int
    ): ObjectAnalysisResult = ObjectAnalysisResult(
        id = id,
        objectTitle = "Monocrystalline Solar Cell",
        scientificOrTechnicalName = "Silicon P-N Junction Photovoltaic Module",
        category = "Energy",
        summary = "Sample Guide: A photovoltaic solar cell converts photons directly into direct-current electricity with no moving parts using doped crystalline silicon layers.",
        languageCode = "en",
        structures = listOf(
            StructurePart(
                layerOrder = 1,
                name = "Tempered Anti-Reflective Glass",
                materialOrRole = "Low-iron toughened glass + textured coating",
                functionDescription = "Protects fragile wafers from hail and UV while trapping incoming sunlight instead of reflecting it."
            ),
            StructurePart(
                layerOrder = 2,
                name = "N-Type & P-Type Silicon Junction",
                materialOrRole = "Phosphorus- and boron-doped monocrystalline silicon",
                functionDescription = "Creates an internal electric field that separates light-freed electrons from positively charged holes."
            ),
            StructurePart(
                layerOrder = 3,
                name = "Silver Grid Fingers & Busbars",
                materialOrRole = "Conductive metallic micro-ribbons",
                functionDescription = "Collects freed electrons across the wafer surface with minimal shading."
            ),
            StructurePart(
                layerOrder = 4,
                name = "EVA Encapsulant & Backsheet",
                materialOrRole = "Polymer barrier & aluminum frame",
                functionDescription = "Seals out humidity for 25+ years and dissipates excess thermal energy."
            )
        ),
        facts = listOf(
            EducationalFact(
                discipline = "Quantum Physics",
                title = "The Photovoltaic Effect",
                detail = "Discovered by Edmond Becquerel in 1839 and explained via quantum light packets by Einstein in 1905—only photons above Silicon's 1.1 eV bandgap free electrons."
            ),
            EducationalFact(
                discipline = "Thermodynamics",
                title = "Cooler Panels Produce More Voltage",
                detail = "Solar panels rely on light, not heat—every 1°C rise above 25°C typically lowers output power by ~0.35%."
            )
        ),
        solutions = listOf(
            PracticalSolution(
                problemTitle = "Low Output on Sunny Afternoons",
                solutionSteps = "Check for partial tree shading across even one cell (which throttles a whole series string) and clean dust/pollen film gently with soft water.",
                careOrSustainabilityTip = "Tilt panels near your local latitude angle and leave airflow space underneath to keep cell temperatures lower."
            ),
            PracticalSolution(
                problemTitle = "Off-Grid Battery Not Charging",
                solutionSteps = "Verify MPPT charge controller fuse continuity, check polarity at MC4 connectors, and measure open-circuit voltage in full sun.",
                careOrSustainabilityTip = "Keep off-grid LiFePO4 or AGM batteries shaded and ventilated to double cycle lifespan."
            )
        ),
        quiz = QuickQuizQuestion(
            question = "What happens to a silicon solar panel's voltage efficiency as its temperature climbs far above 25°C?",
            options = listOf(
                "Efficiency increases rapidly",
                "Efficiency drops slightly due to higher resistance",
                "It switches to AC automatically",
                "Nothing changes at all"
            ),
            correctIndex = 1,
            explanation = "Solar cells convert light photons, not heat; elevated temperatures increase carrier recombination and reduce voltage."
        ),
        imageSource = imageSource ?: "drawable://img_sample_solar",
        estimatedPayloadKb = payloadKb,
        isSamplePlaceholder = true
    )

    private fun createWaterFilterSample(
        imageSource: String?,
        payloadKb: Int
    ): ObjectAnalysisResult = ObjectAnalysisResult(
        id = "study_${UUID.randomUUID().toString().take(8)}",
        objectTitle = "Ceramic & Carbon Water Filter",
        scientificOrTechnicalName = "Multi-Stage Porous Filtration & Adsorption Assembly",
        category = "Everyday",
        summary = "Sample Guide: Gravity and countertop water filters combine mechanical micro-sieving with chemical adsorption to make drinking water safer in both homes and low-resource regions.",
        languageCode = "en",
        structures = listOf(
            StructurePart(
                layerOrder = 1,
                name = "Micro-Porous Ceramic Shell",
                materialOrRole = "Kiln-fired clay with 0.2–0.5 micron pores",
                functionDescription = "Physically blocks suspended silt, protozoa, and most bacteria while letting water molecules pass."
            ),
            StructurePart(
                layerOrder = 2,
                name = "Activated Coconut-Shell Carbon Core",
                materialOrRole = "High-surface-area nanoporous charcoal",
                functionDescription = "Adsorbs chlorine, volatile organics, pesticides, and odor-causing compounds."
            ),
            StructurePart(
                layerOrder = 3,
                name = "Colloidal Silver Barrier",
                materialOrRole = "Trace antimicrobial impregnation",
                functionDescription = "Inhibits biofilm growth inside the ceramic pores during storage between uses."
            )
        ),
        facts = listOf(
            EducationalFact(
                discipline = "Surface Chemistry",
                title = "A Teaspoon with a Football Field of Area",
                detail = "One gram of activated carbon contains up to 1,500–3,000 square meters of internal pore surface area for trapping dissolved molecules."
            ),
            EducationalFact(
                discipline = "Public Health",
                title = "Zero-Electricity Purification",
                detail = "Gravity ceramic pot filters operate completely offline without pumps or grids—reducing waterborne illness in rural communities."
            )
        ),
        solutions = listOf(
            PracticalSolution(
                problemTitle = "Slow Drip / Clogged Flow Rate",
                solutionSteps = "Gently scrub the outer ceramic wall with a clean soft brush under filtered water to remove trapped silt—never use soap.",
                careOrSustainabilityTip = "Let muddy source water settle in a pre-bucket for 30 minutes before pouring into the ceramic chamber."
            )
        ),
        quiz = QuickQuizQuestion(
            question = "Why is activated carbon effective at removing chemical odors and chlorine?",
            options = listOf(
                "It boils the water",
                "Its vast microscopic pore surface adsorbs dissolved molecules",
                "It freezes bacteria",
                "It adds salt ions"
            ),
            correctIndex = 1,
            explanation = "Activated carbon traps dissolved organic and chlorine molecules on its immense internal surface area via adsorption."
        ),
        imageSource = imageSource,
        estimatedPayloadKb = payloadKb,
        isSamplePlaceholder = true
    )

    private fun createElectricMotorSample(
        imageSource: String?,
        payloadKb: Int
    ): ObjectAnalysisResult = ObjectAnalysisResult(
        id = "study_${UUID.randomUUID().toString().take(8)}",
        objectTitle = "Brushless DC Electric Motor",
        scientificOrTechnicalName = "Electromagnetic Stator-Rotor Transducer",
        category = "Engineering",
        summary = "Sample Guide: An electric motor turns electrical current into smooth rotational torque using opposing and attracting magnetic fields between stationary copper coils and a spinning magnet rotor.",
        languageCode = "en",
        structures = listOf(
            StructurePart(
                layerOrder = 1,
                name = "Laminated Copper Stator",
                materialOrRole = "Enamel-insulated copper windings on silicon steel",
                functionDescription = "Generates a precisely sequenced rotating magnetic field when energized."
            ),
            StructurePart(
                layerOrder = 2,
                name = "Permanent Magnet Rotor & Shaft",
                materialOrRole = "Neodymium magnets around hardened steel axle",
                functionDescription = "Chases the stator's rotating magnetic field to deliver continuous mechanical torque."
            ),
            StructurePart(
                layerOrder = 3,
                name = "Sealed Ball Bearings",
                materialOrRole = "Precision steel races",
                functionDescription = "Supports radial and axial shaft loads with minimal heat and noise."
            )
        ),
        facts = listOf(
            EducationalFact(
                discipline = "Electromagnetism",
                title = "Lorentz Force in Action",
                detail = "Current flowing perpendicular to a magnetic field experiences a sideways force—discovered by Michael Faraday and quantified by Hendrik Lorentz."
            ),
            EducationalFact(
                discipline = "Efficiency",
                title = "Over 85% Energy Conversion",
                detail = "Unlike internal combustion engines (~25–35% efficient), modern brushless electric motors convert 85–95% of electrical energy into motion."
            )
        ),
        solutions = listOf(
            PracticalSolution(
                problemTitle = "Motor Overheating or Whining",
                solutionSteps = "Remove dust/hair wrapped around the shaft, verify cooling vents are clear, and check that the load turns freely by hand when unpowered.",
                careOrSustainabilityTip = "Always disconnect power before clearing jammed blades or inspecting bearings."
            )
        ),
        quiz = QuickQuizQuestion(
            question = "Why are stator cores built from thin stacked steel sheets instead of one solid iron block?",
            options = listOf(
                "To make the motor heavier",
                "To reduce wasteful eddy current heating",
                "To store battery charge",
                "To insulate copper from oxygen"
            ),
            correctIndex = 1,
            explanation = "Thin insulated laminations block large circulating eddy currents, keeping the motor cool and efficient."
        ),
        imageSource = imageSource,
        estimatedPayloadKb = payloadKb,
        isSamplePlaceholder = true
    )

    private fun createCustomSearchSample(
        queryTitle: String,
        imageSource: String?,
        payloadKb: Int
    ): ObjectAnalysisResult = ObjectAnalysisResult(
        id = "study_${UUID.randomUUID().toString().take(8)}",
        objectTitle = queryTitle,
        scientificOrTechnicalName = "Sample Structural & Functional Breakdown",
        category = "Everyday",
        summary = "Sample Guide for \"$queryTitle\": Every physical object can be understood by examining its outer shell, load-bearing core, functional interface, and maintenance lifecycle. Enable Firebase AI Logic for live photo-specific identification.",
        languageCode = "en",
        structures = listOf(
            StructurePart(
                layerOrder = 1,
                name = "Protective Outer Shell",
                materialOrRole = "Surface barrier / ergonomic enclosure",
                functionDescription = "Shields internal mechanisms of $queryTitle from moisture, impact, and UV wear while enabling safe human handling."
            ),
            StructurePart(
                layerOrder = 2,
                name = "Primary Structural Core",
                materialOrRole = "Load-bearing frame or active medium",
                functionDescription = "Maintains geometry under stress and anchors sub-assemblies together."
            ),
            StructurePart(
                layerOrder = 3,
                name = "Functional Energy / Signal Interface",
                materialOrRole = "Active working element",
                functionDescription = "Where energy, force, or information transforms to perform the primary task of $queryTitle."
            )
        ),
        facts = listOf(
            EducationalFact(
                discipline = "Engineering Design",
                title = "Form Follows Material Constraints",
                detail = "Engineers select materials for $queryTitle by balancing tensile strength, thermal conductivity, weight, and cost."
            ),
            EducationalFact(
                discipline = "Sustainability",
                title = "Modular Repairability",
                detail = "Objects assembled with mechanical fasteners rather than permanent adhesives last far longer and are easier to recycle."
            )
        ),
        solutions = listOf(
            PracticalSolution(
                problemTitle = "Preventing Premature Wear",
                solutionSteps = "Keep moving joints or contact surfaces clean, dry, and free of abrasive dust; inspect seals or fasteners periodically.",
                careOrSustainabilityTip = "Saving this guide offline preserves structural notes and troubleshooting steps even without internet."
            )
        ),
        quiz = QuickQuizQuestion(
            question = "Why is inspecting an object's outer shell and joints useful before disassembly?",
            options = listOf(
                "It shows load paths and fastener access safely",
                "It increases weight",
                "It erases serial numbers",
                "It disables gravity"
            ),
            correctIndex = 0,
            explanation = "Identifying fasteners, seals, and load-bearing seams prevents accidental damage during care or repair."
        ),
        imageSource = imageSource,
        estimatedPayloadKb = payloadKb,
        isSamplePlaceholder = true
    )

    private fun createPhotographedSample(
        imageSource: String,
        payloadKb: Int
    ): ObjectAnalysisResult = createCustomSearchSample(
        queryTitle = "Captured Object Study",
        imageSource = imageSource,
        payloadKb = payloadKb
    ).copy(
        scientificOrTechnicalName = "Uploaded Photo · Sample Visual Analysis",
        summary = "Sample Guide: Your photo was compressed locally to $payloadKb KB for low-bandwidth readiness. Live visual recognition of the exact item in your photo requires enabling Firebase AI Logic; below is a sample structural learning template."
    )

    private fun localizeSampleResult(
        result: ObjectAnalysisResult,
        targetLanguage: StudyLanguage
    ): ObjectAnalysisResult {
        if (targetLanguage.code == "en") {
            return result.copy(languageCode = "en")
        }

        val localizedPrefix = when (targetLanguage.code) {
            "es" -> "[Muestra · Español] Guía educativa de ${result.objectTitle}: "
            "fr" -> "[Exemple · Français] Guide éducatif de ${result.objectTitle} : "
            "ar" -> "[نموذج · العربية] دليل تعليمي عن ${result.objectTitle}: "
            "hi" -> "[नमूना · हिन्दी] ${result.objectTitle} की संरचना, तथ्य और समाधान: "
            "zh" -> "[示例 · 中文] ${result.objectTitle} 的结构解析与科普指南："
            "bn" -> "[নমুনা · বাংলা] ${result.objectTitle} সম্পর্কে শিক্ষামূলক বিশ্লেষণ: "
            "pt" -> "[Exemplo · Português] Guia educativo de ${result.objectTitle}: "
            "sw" -> "[Mfano · Kiswahili] Mwongozo wa kielimu kuhusu ${result.objectTitle}: "
            "ja" -> "[サンプル · 日本語] ${result.objectTitle} の構造と学習ガイド："
            "de" -> "[Beispiel · Deutsch] Lernleitfaden zu ${result.objectTitle}: "
            "ko" -> "[샘플 · 한국어] ${result.objectTitle} 구조 및 학습 가이드: "
            "id" -> "[Contoh · Bahasa Indonesia] Panduan edukasi ${result.objectTitle}: "
            "ru" -> "[Пример · Русский] Учебный разбор (${result.objectTitle}): "
            "tr" -> "[Örnek · Türkçe] ${result.objectTitle} için eğitim rehberi: "
            else -> "[Sample · ${targetLanguage.nativeName}] "
        }

        val localizedSummaryTail = when (targetLanguage.code) {
            "es" -> "Explora sus capas estructurales, principios científicos y soluciones prácticas. (La traducción dinámica completa con IA requiere Firebase)."
            "fr" -> "Découvrez ses couches structurelles, faits scientifiques et solutions pratiques. (La traduction IA dynamique nécessite Firebase)."
            "ar" -> "استكشف الطبقات البنيوية والحقائق العلمية والحلول العملية. (الترجمة الديناميكية الكاملة بالذكاء الاصطناعي تتطلب Firebase)."
            "hi" -> "इसकी संरचनात्मक परतों, वैज्ञानिक तथ्यों और व्यावहारिक समाधानों का अध्ययन करें। (पूर्ण लाइव AI अनुवाद के लिए Firebase आवश्यक है)।"
            "zh" -> "了解其结构组成、科学原理与实用维护方案。（完整实时 AI 翻译需启用 Firebase）。"
            "bn" -> "এর গঠনস্তর, বৈজ্ঞানিক তথ্য এবং ব্যবহারিক সমাধান জানুন। (সম্পূর্ণ লাইভ AI অনুবাদের জন্য Firebase প্রয়োজন)।"
            "pt" -> "Explore suas camadas estruturais, fatos científicos e soluções práticas. (Tradução dinâmica por IA requer Firebase)."
            "sw" -> "Chunguza muundo wake, ukweli wa kisayansi, na suluhisho za vitendo. (Tafsiri kamili ya AI inahitaji Firebase)."
            "ja" -> "各構造、科学的な豆知識、実用的な解決策を学べます。（リアルタイムAI翻訳にはFirebaseが必要です）。"
            "de" -> "Entdecken Sie Aufbau, wissenschaftliche Fakten und praktische Lösungen. (Dynamische KI-Übersetzung erfordert Firebase)."
            "ko" -> "구조적 층위, 과학적 사실, 실용적 해결책을 살펴보세요. (실시간 AI 번역은 Firebase가 필요합니다)."
            "id" -> "Pelajari struktur lapisan, fakta ilmiah, dan solusi praktisnya. (Terjemahan AI dinamis memerlukan Firebase)."
            "ru" -> "Изучите внутреннюю структуру, научные факты и практические решения. (Полный динамический ИИ-перевод требует Firebase)."
            "tr" -> "Yapısal katmanlarını, bilimsel gerçekleri ve pratik çözümleri inceleyin. (Tam canlı yapay zeka çevirisi Firebase gerektirir)."
            else -> result.summary
        }

        val cleanBaseSummary = result.summary
            .substringAfter("]: ", result.summary)
            .substringAfter(": ", result.summary)

        return result.copy(
            languageCode = targetLanguage.code,
            summary = "$localizedPrefix$localizedSummaryTail\n\n$cleanBaseSummary"
        )
    }

    private fun String.containsAny(vararg keywords: String): Boolean =
        keywords.any { this.contains(it) }
}
