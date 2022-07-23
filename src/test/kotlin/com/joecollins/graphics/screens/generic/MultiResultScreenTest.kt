package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.components.MapFrameTest
import com.joecollins.graphics.screens.generic.MultiResultScreen.Companion.of
import com.joecollins.graphics.screens.generic.MultiResultScreen.Companion.ofParties
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.graphics.utils.ShapefileReader.readShapes
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import org.junit.Test
import java.awt.Color
import java.awt.Shape
import java.util.concurrent.Flow

class MultiResultScreenTest {

    @Test
    fun testSimplePanel() {
        val districts = ArrayList<District>()
        districts.add(
            District(
                8,
                "Stanhope-Marshfield",
                mapOf(
                    Candidate("Wade MacLauchlan", lib, true) to 1196,
                    Candidate("Bloyce Thompson", pc) to 1300,
                    Candidate("Sarah Donald", grn) to 747,
                    Candidate("Marian White", ndp) to 46
                ),
                false,
                mapOf(
                    lib to 1938,
                    pc to 1338,
                    grn to 347,
                    ndp to 443
                )
            )
        )
        districts.add(
            District(
                15,
                "Brackley-Hunter River",
                mapOf(
                    Candidate("Windsor Wight", lib) to 899,
                    Candidate("Dennis King", pc, true) to 1315,
                    Candidate("Greg Bradley", grn) to 879,
                    Candidate("Leah-Jane Hayward", ndp) to 57
                ),
                true,
                mapOf(
                    lib to 1389,
                    pc to 1330,
                    grn to 462,
                    ndp to 516
                )
            )
        )
        districts.add(
            District(
                17,
                "New Haven-Rocky Point",
                mapOf(
                    Candidate("Judy MacNevin", lib) to 515,
                    Candidate("Kris Currie", pc) to 1068,
                    Candidate("Peter Bevan-Baker", grn, true) to 1870,
                    Candidate("Don Wills", ind) to 26
                ),
                true,
                mapOf(
                    lib to 1046,
                    pc to 609,
                    grn to 2077,
                    ndp to 58
                )
            )
        )
        val shapesByDistrict = peiShapesByDistrict()
        val swingometerOrder = listOf(ndp, grn, lib, ind, pc)
        val panel = of(
            districts.asOneTimePublisher(),
            { it.votes.asOneTimePublisher() },
            { ("DISTRICT " + it.districtNum).asOneTimePublisher() },
            { it.name.uppercase().asOneTimePublisher() }
        )
            .withIncumbentMarker("(MLA)")
            .withWinner { (if (it.leaderHasWon) it.votes.entries.maxByOrNull { e -> e.value }!!.key else null).asOneTimePublisher() }
            .withPrev(
                { it.prevVotes.asOneTimePublisher() },
                { "SWING SINCE 2015".asOneTimePublisher() },
                compareBy { swingometerOrder.indexOf(it) }
            )
            .withMap(
                { shapesByDistrict },
                { it.districtNum },
                { PartyResult(it.votes.entries.maxByOrNull { e -> e.value }?.key?.party, it.leaderHasWon).asOneTimePublisher() },
                { if (it.districtNum < 10) listOf(1, 2, 3, 4, 5, 6, 7, 8) else listOf(15, 16, 17, 18, 19, 20) },
                { (if (it.districtNum < 10) "CARDIGAN" else "MALPEQUE").asOneTimePublisher() }
            )
            .build("PARTY LEADERS".asOneTimePublisher())
        panel.setSize(1024, 512)
        compareRendering("MultiResultPanel", "Basic-1", panel, 2)
    }

    @Test
    fun testVariousUpdates() {
        val district8 = District(
            8,
            "Stanhope-Marshfield",
            mapOf(
                Candidate("Sarah Donald", grn) to 0,
                Candidate("Wade MacLauchlan", lib, true) to 0,
                Candidate("Bloyce Thompson", pc) to 0,
                Candidate("Marian White", ndp) to 0
            ),
            false,
            mapOf(
                lib to 1938,
                pc to 1338,
                grn to 347,
                ndp to 443
            ),
            "0 OF 10 POLLS REPORTING",
            0.0
        )
        val district15 = District(
            15,
            "Brackley-Hunter River",
            mapOf(
                Candidate("Greg Bradley", grn) to 0,
                Candidate("Leah-Jane Hayward", ndp) to 0,
                Candidate("Dennis King", pc, true) to 0,
                Candidate("Windsor Wight", lib) to 0
            ),
            false,
            mapOf(
                lib to 1389,
                pc to 1330,
                grn to 462,
                ndp to 516
            ),
            "0 OF 10 POLLS REPORTING",
            0.0
        )
        val district17 = District(
            17,
            "New Haven-Rocky Point",
            mapOf(
                Candidate("Peter Bevan-Baker", grn, true) to 0,
                Candidate("Kris Currie", pc) to 0,
                Candidate("Judy MacNevin", lib) to 0,
                Candidate("Don Wills", ind) to 0
            ),
            false,
            mapOf(
                lib to 1046,
                pc to 609,
                grn to 2077,
                ndp to 58
            ),
            "0 OF 10 POLLS REPORTING",
            0.0
        )
        val districts = Publisher(listOf(district15, district17))
        val shapesByDistrict = peiShapesByDistrict()
        val swingometerOrder = listOf(ndp, grn, lib, ind, pc)
        val title = Publisher("MAJOR PARTY LEADERS")
        val panel = of(
            districts,
            { it.getVotes() },
            { it.name.uppercase().asOneTimePublisher() },
            { it.getStatus() }
        )
            .withIncumbentMarker("(MLA)")
            .withPctReporting { it.getPctReporting() }
            .withWinner { it.winner }
            .withPrev(
                { it.prevVotes.asOneTimePublisher() },
                { "SWING SINCE 2015".asOneTimePublisher() },
                compareBy { swingometerOrder.indexOf(it) }
            )
            .withMap(
                { shapesByDistrict },
                { it.districtNum },
                {
                    it.leader.map { e ->
                        if (e == null)
                            null
                        else
                            PartyResult(e.first.party, e.second)
                    }
                },
                { if (it.districtNum < 10) listOf(1, 2, 3, 4, 5, 6, 7, 8) else listOf(15, 16, 17, 18, 19, 20) },
                { (if (it.districtNum < 10) "CARDIGAN" else "MALPEQUE").asOneTimePublisher() }
            )
            .build(title)
        panel.setSize(1024, 512)
        compareRendering("MultiResultPanel", "Update-1", panel)
        district17.update(
            "1 OF 10 POLLS REPORTING",
            0.1,
            mapOf(
                Candidate("Peter Bevan-Baker", grn, true) to 851,
                Candidate("Kris Currie", pc) to 512,
                Candidate("Judy MacNevin", lib) to 290,
                Candidate("Don Wills", ind) to 7
            )
        )
        compareRendering("MultiResultPanel", "Update-2", panel)
        districts.submit(listOf(district8, district15, district17))
        title.submit("PARTY LEADERS")
        compareRendering("MultiResultPanel", "Update-3", panel)
        district15.update(
            "1 OF 10 POLLS REPORTING",
            0.1,
            mapOf(
                Candidate("Greg Bradley", grn) to 287,
                Candidate("Leah-Jane Hayward", ndp) to 27,
                Candidate("Dennis King", pc, true) to 583,
                Candidate("Windsor Wight", lib) to 425
            )
        )
        compareRendering("MultiResultPanel", "Update-4", panel)
        district8.update(
            "1 OF 10 POLLS REPORTING",
            0.1,
            mapOf(
                Candidate("Sarah Donald", grn) to 285,
                Candidate("Wade MacLauchlan", lib, true) to 620,
                Candidate("Bloyce Thompson", pc) to 609,
                Candidate("Marian White", ndp) to 22
            )
        )
        compareRendering("MultiResultPanel", "Update-5", panel)
        district15.update(
            "5 OF 10 POLLS REPORTING",
            0.5,
            mapOf(
                Candidate("Greg Bradley", grn) to 287 + 72 + 91 + 79 + 38,
                Candidate("Leah-Jane Hayward", ndp) to 27 + 7 + 7 + 1 + 3,
                Candidate("Dennis King", pc, true) to 583 + 87 + 109 + 76 + 54,
                Candidate("Windsor Wight", lib) to 425 + 73 + 66 + 58 + 30
            ),
            true
        )
        compareRendering("MultiResultPanel", "Update-6", panel)
        districts.submit(listOf(district8, district17))
        title.submit("PARTY LEADERS IN DOUBT")
        compareRendering("MultiResultPanel", "Update-7", panel)
        district15.update(
            "10 OF 10 POLLS REPORTING",
            1.0,
            mapOf(
                Candidate("Greg Bradley", grn) to 879,
                Candidate("Leah-Jane Hayward", ndp) to 57,
                Candidate("Dennis King", pc, true) to 1315,
                Candidate("Windsor Wight", lib) to 899
            )
        )
        // intentionally same as before, as this district is no longer displayed
        compareRendering("MultiResultPanel", "Update-7", panel)
        district17.update(
            "2 OF 10 POLLS REPORTING",
            0.2,
            mapOf(
                Candidate("Peter Bevan-Baker", grn, true) to 851 + 117,
                Candidate("Kris Currie", pc) to 512 + 90,
                Candidate("Judy MacNevin", lib) to 290 + 28,
                Candidate("Don Wills", ind) to 7 + 4
            )
        )
        compareRendering("MultiResultPanel", "Update-8", panel)
        district8.update(
            "2 OF 10 POLLS REPORTING",
            0.2,
            mapOf(
                Candidate("Sarah Donald", grn) to 285 + 50,
                Candidate("Wade MacLauchlan", lib, true) to 620 + 68,
                Candidate("Bloyce Thompson", pc) to 609 + 112,
                Candidate("Marian White", ndp) to 22 + 7
            )
        )
        compareRendering("MultiResultPanel", "Update-9", panel)
    }

    @Test
    fun testOthersPanel() {
        val districts = ArrayList<District>()
        districts.add(
            District(
                30,
                "Saint John East",
                mapOf(
                    Candidate("Glen Savoie", pc, true) to 3507,
                    Candidate("Phil Comeau", lib) to 1639,
                    Candidate("Gerald Irish", grn) to 394,
                    Candidate("Patrick Kemp", pa) to 434,
                    Candidate("Josh Floyd", ndp) to 248
                ),
                true,
                mapOf(
                    lib to 1775,
                    pc to 3017,
                    grn to 373,
                    ndp to 402,
                    pa to 1047
                )
            )
        )
        districts.add(
            District(
                32,
                "Saint John Harbour",
                mapOf(
                    Candidate("Arlene Dunn", pc) to 2181,
                    Candidate("Alice McKim", lib) to 1207,
                    Candidate("Brent Harris", grn) to 1224,
                    Candidate("Tony Gunn", pa) to 186,
                    Candidate("Courtney Pyrke", ndp) to 309,
                    Candidate("Mike Cyr", ind) to 47,
                    Candidate("Arty Watson", ind) to 114
                ),
                false,
                mapOf(
                    lib to 1865,
                    pc to 1855,
                    grn to 721,
                    ndp to 836,
                    pa to 393
                )
            )
        )
        districts.add(
            District(
                33,
                "Saint John Lancaster",
                mapOf(
                    Candidate("Dorothy Shephard", pc, true) to 3560,
                    Candidate("Sharon Teare", lib) to 1471,
                    Candidate("Joanna Killen", grn) to 938,
                    Candidate("Paul Seelye", pa) to 394,
                    Candidate("Don Durant", ndp) to 201
                ),
                true,
                mapOf(
                    lib to 1727,
                    pc to 3001,
                    grn to 582,
                    ndp to 414,
                    pa to 922
                )
            )
        )
        val swingometerOrder = listOf(ndp, grn, lib, ind, pc, pa)
        val panel = of(
            districts.asOneTimePublisher(),
            { it.votes.asOneTimePublisher() },
            { ("DISTRICT " + it.districtNum).asOneTimePublisher() },
            { it.name.uppercase().asOneTimePublisher() }
        )
            .withIncumbentMarker("(MLA)")
            .withWinner { d ->
                (
                    if (d.leaderHasWon)
                        d.votes.entries.maxByOrNull { it.value }!!.key
                    else
                        null
                    ).asOneTimePublisher()
            }
            .withPrev(
                { it.prevVotes.asOneTimePublisher() },
                { "SWING SINCE 2018".asOneTimePublisher() },
                compareBy { swingometerOrder.indexOf(it) }
            )
            .build("SAINT JOHN".asOneTimePublisher())
        panel.setSize(1024, 512)
        compareRendering("MultiResultPanel", "Others-1", panel)
    }

    @Test
    fun testOthersPartyPanel() {
        val districts = ArrayList<District>()
        districts.add(
            District(
                30,
                "Saint John East",
                mapOf(
                    Candidate("Glen Savoie", pc, true) to 3507,
                    Candidate("Phil Comeau", lib) to 1639,
                    Candidate("Gerald Irish", grn) to 394,
                    Candidate("Patrick Kemp", pa) to 434,
                    Candidate("Josh Floyd", ndp) to 248
                ),
                true,
                mapOf(
                    lib to 1775,
                    pc to 3017,
                    grn to 373,
                    ndp to 402,
                    pa to 1047
                )
            )
        )
        districts.add(
            District(
                32,
                "Saint John Harbour",
                mapOf(
                    Candidate("Arlene Dunn", pc) to 2181,
                    Candidate("Alice McKim", lib) to 1207,
                    Candidate("Brent Harris", grn) to 1224,
                    Candidate("Courtney Pyrke", ndp) to 309,
                    Candidate("Others", Party.OTHERS) to 47 + 114 + 186
                ),
                false,
                mapOf(
                    lib to 1865,
                    pc to 1855,
                    grn to 721,
                    ndp to 836,
                    pa to 393
                )
            )
        )
        districts.add(
            District(
                33,
                "Saint John Lancaster",
                mapOf(
                    Candidate("Dorothy Shephard", pc, true) to 3560,
                    Candidate("Sharon Teare", lib) to 1471,
                    Candidate("Joanna Killen", grn) to 938,
                    Candidate("Paul Seelye", pa) to 394,
                    Candidate("Don Durant", ndp) to 201
                ),
                true,
                mapOf(
                    lib to 1727,
                    pc to 3001,
                    grn to 582,
                    ndp to 414,
                    pa to 922
                )
            )
        )
        districts.add(
            District(
                38,
                "Fredericton-Grand Lake",
                mapOf(
                    Candidate("Mary E. Wilson", pc, true) to 3374,
                    Candidate("Steven Burns", lib) to 2072,
                    Candidate("Gail Costello", grn) to 1306,
                    Candidate("Craig Rector", pa) to 745,
                    Candidate("Natasha M Akhtar", ndp) to 127
                ),
                true,
                mapOf(
                    lib to 955,
                    pc to 2433,
                    grn to 472,
                    ndp to 114,
                    pa to 4799
                )
            )
        )
        districts.add(
            District(
                40,
                "Fredericton South",
                mapOf(
                    Candidate("Brian MacKinnon", pc) to 2342,
                    Candidate("Nicole Picot", lib) to 895,
                    Candidate("David Coon", grn, true) to 4213,
                    Candidate("Wendell Betts", pa) to 234,
                    Candidate("Geoffrey Noseworthy", ndp) to 117
                ),
                true,
                mapOf(
                    lib to 1525,
                    pc to 1042,
                    grn to 4273,
                    ndp to 132,
                    pa to 616
                )
            )
        )
        districts.add(
            District(
                41,
                "Fredericton North",
                mapOf(
                    Candidate("Jill Green", pc) to 3227,
                    Candidate("Stephen Horsman", lib, true) to 1464,
                    Candidate("Luke Randall", grn) to 2464,
                    Candidate("Allen Price", pa) to 591,
                    Candidate("Mackenzie Thomason", ndp) to 100
                ),
                true,
                mapOf(
                    lib to 2443,
                    pc to 2182,
                    grn to 1313,
                    ndp to 139,
                    pa to 1651
                )
            )
        )
        val swingometerOrder = listOf(ndp, grn, lib, ind, pc, pa)
        val panel = ofParties(
            districts.asOneTimePublisher(),
            { it.votes.mapKeys { e -> e.key.party }.asOneTimePublisher() },
            { ("DISTRICT " + it.districtNum).asOneTimePublisher() },
            { it.name.uppercase().asOneTimePublisher() }
        )
            .withPrev(
                { it.prevVotes.asOneTimePublisher() },
                { "SWING SINCE 2018".asOneTimePublisher() },
                compareBy { swingometerOrder.indexOf(it) }
            )
            .build("SAINT JOHN".asOneTimePublisher())
        panel.setSize(1024, 512)
        compareRendering("MultiResultPanel", "OthersParty-1", panel)
    }

    @Test
    fun testMultipleRowsPanel() {
        val districts = ArrayList<District>()
        districts.add(
            District(
                30,
                "Saint John East",
                mapOf(
                    Candidate("Glen Savoie", pc, true) to 3507,
                    Candidate("Phil Comeau", lib) to 1639,
                    Candidate("Gerald Irish", grn) to 394,
                    Candidate("Patrick Kemp", pa) to 434,
                    Candidate("Josh Floyd", ndp) to 248
                ),
                true,
                mapOf(
                    lib to 1775,
                    pc to 3017,
                    grn to 373,
                    ndp to 402,
                    pa to 1047
                )
            )
        )
        districts.add(
            District(
                31,
                "Portland-Simonds",
                mapOf(
                    Candidate("Trevor Holder", pc, true) to 3170,
                    Candidate("Tim Jones", lib) to 1654,
                    Candidate("Stefan Warner", grn) to 483,
                    Candidate("Darella Jackson", pa) to 282,
                    Candidate("Erik Heinze-Milne", ndp) to 164
                ),
                true,
                mapOf(
                    lib to 1703,
                    pc to 3168,
                    grn to 435,
                    ndp to 449,
                    ind to 191
                )
            )
        )
        districts.add(
            District(
                32,
                "Saint John Harbour",
                mapOf(
                    Candidate("Arlene Dunn", pc) to 2181,
                    Candidate("Alice McKim", lib) to 1207,
                    Candidate("Brent Harris", grn) to 1224,
                    Candidate("Tony Gunn", pa) to 186,
                    Candidate("Courtney Pyrke", ndp) to 309,
                    Candidate("Mike Cyr", ind) to 47,
                    Candidate("Arty Watson", ind) to 114
                ),
                false,
                mapOf(
                    lib to 1865,
                    pc to 1855,
                    grn to 721,
                    ndp to 836,
                    pa to 393
                )
            )
        )
        districts.add(
            District(
                33,
                "Saint John Lancaster",
                mapOf(
                    Candidate("Dorothy Shephard", pc, true) to 3560,
                    Candidate("Sharon Teare", lib) to 1471,
                    Candidate("Joanna Killen", grn) to 938,
                    Candidate("Paul Seelye", pa) to 394,
                    Candidate("Don Durant", ndp) to 201
                ),
                true,
                mapOf(
                    lib to 1727,
                    pc to 3001,
                    grn to 582,
                    ndp to 414,
                    pa to 922
                )
            )
        )
        val swingometerOrder = listOf(ndp, grn, lib, ind, pc, pa)
        val districtsPublisher = Publisher(districts)
        val panel = of(
            districtsPublisher,
            { it.votes.asOneTimePublisher() },
            { ("DISTRICT " + it.districtNum).asOneTimePublisher() },
            { it.name.uppercase().asOneTimePublisher() }
        )
            .withIncumbentMarker("(MLA)")
            .withWinner { d ->
                (
                    if (d.leaderHasWon)
                        d.votes.entries.maxByOrNull { it.value }!!.key
                    else
                        null
                    ).asOneTimePublisher()
            }
            .withPrev(
                { it.prevVotes.asOneTimePublisher() },
                { "SWING SINCE 2018".asOneTimePublisher() },
                compareBy { swingometerOrder.indexOf(it) }
            )
            .build("ELECTION 2020: NEW BRUNSWICK DECIDES".asOneTimePublisher())
        panel.setSize(1024, 512)
        compareRendering("MultiResultPanel", "MultipleRows-1", panel)
        districts.add(
            District(
                34,
                "Kings Centre",
                mapOf(
                    Candidate("Bill Oliver", pc, true) to 4583,
                    Candidate("Bruce Bryer", grn) to 1006,
                    Candidate("Paul Adams", lib) to 911,
                    Candidate("William Edgett", pa) to 693,
                    Candidate("Margaret Anderson Kilfoil", ndp) to 254
                ),
                true,
                mapOf(
                    lib to 1785,
                    pc to 3267,
                    grn to 731,
                    ndp to 342,
                    pa to 1454
                )
            )
        )
        districts.add(
            District(
                35,
                "Fundy-The Isles-Saint John West",
                mapOf(
                    Candidate("Andrea Anderson-Mason", pc, true) to 4740,
                    Candidate("Tony Mann", lib) to 726,
                    Candidate("Vincent Edgett", pa) to 688,
                    Candidate("Lois Mitchell", grn) to 686,
                    Candidate("Sharon Greenlaw", ndp) to 291
                ),
                true,
                mapOf(
                    lib to 2422,
                    pc to 3808,
                    grn to 469,
                    ndp to 203,
                    pa to 1104
                )
            )
        )
        districtsPublisher.submit(districts)
        compareRendering("MultiResultPanel", "MultipleRows-2", panel)
        districts.removeAt(3)
        districts.removeAt(2)
        districts.removeAt(1)
        districts.removeAt(0)
        districtsPublisher.submit(districts)
        compareRendering("MultiResultPanel", "MultipleRows-3", panel)
    }

    @Test
    fun testRunoffs() {
        val districts = ArrayList<District>()
        districts.add(
            District(
                2,
                "Regular Election",
                mapOf(
                    Candidate("David Perdue", gop) to 2458453,
                    Candidate("Jon Ossoff", dem) to 2371921,
                    Candidate("Shane Hazel", ind) to 114873
                ),
                false,
                emptyMap()
            )
        )
        districts.add(
            District(
                3,
                "Special Election",
                mapOf(
                    Candidate("Raphael Warnock", dem) to 1615550,
                    Candidate("Kelly Loeffler", gop) to 1271320,
                    Candidate("Doug Collins", gop) to 979052,
                    Candidate("Deborah Jackson", dem) to 323833,
                    Candidate.OTHERS to 718808
                ),
                false,
                emptyMap()
            )
        )
        val panel = of(
            districts.asOneTimePublisher(),
            { it.votes.asOneTimePublisher() },
            { it.name.uppercase().asOneTimePublisher() },
            { ("CLASS " + it.districtNum).asOneTimePublisher() }
        )
            .withRunoff { it.runoff }
            .build("GEORGIA SENATE".asOneTimePublisher())
        panel.setSize(1024, 512)
        compareRendering("MultiResultPanel", "Runoff-1", panel)
        districts.forEach { it.declareRunoff() }
        compareRendering("MultiResultPanel", "Runoff-2", panel)
    }

    @Test
    fun testMapAdditionalHighlights() {
        val districts = ArrayList<District>()
        districts.add(
            District(
                13,
                "Charlottetown-Brighton",
                mapOf(
                    Candidate("Jordan Brown", lib, true) to 1223,
                    Candidate("Donna Hurry", pc) to 567,
                    Candidate("Ole Hammarlund", grn) to 1301,
                    Candidate("Simone Webster", ndp) to 138
                ),
                false,
                mapOf(
                    lib to 1054,
                    pc to 1032,
                    grn to 352,
                    ndp to 265
                )
            )
        )
        districts.add(
            District(
                12,
                "Charlottetown-Victoria Park",
                mapOf(
                    Candidate("Richard Brown", lib, true) to 875,
                    Candidate("Tim Keizer", pc) to 656,
                    Candidate("Karla Bernard", grn) to 1272,
                    Candidate("Joe Byrne", ndp) to 338
                ),
                false,
                mapOf(
                    lib to 955,
                    pc to 666,
                    grn to 456,
                    ndp to 348
                )
            )
        )
        districts.add(
            District(
                10,
                "Charlottetown-Winsloe",
                mapOf(
                    Candidate("Robert Mitchell", lib, true) to 1420,
                    Candidate("Mike Gillis", pc) to 865,
                    Candidate("Amanda Morrison", grn) to 1057,
                    Candidate("Jesse Reddin Cousins", ndp) to 41
                ),
                false,
                mapOf(
                    lib to 1425,
                    pc to 1031,
                    grn to 295,
                    ndp to 360
                )
            )
        )
        val shapesByDistrict = peiShapesByDistrict()
        val swingometerOrder = listOf(ndp, grn, lib, ind, pc)
        val panel = of(
            districts.asOneTimePublisher(),
            { it.votes.asOneTimePublisher() },
            { ("DISTRICT " + it.districtNum).asOneTimePublisher() },
            { it.name.uppercase().asOneTimePublisher() }
        )
            .withIncumbentMarker("(MLA)")
            .withWinner { d ->
                (
                    if (d.leaderHasWon)
                        d.votes.entries.maxByOrNull { it.value }!!.key
                    else
                        null
                    ).asOneTimePublisher()
            }
            .withPrev(
                { it.prevVotes.asOneTimePublisher() },
                { "SWING SINCE 2015".asOneTimePublisher() },
                compareBy { swingometerOrder.indexOf(it) }
            )
            .withMap(
                { shapesByDistrict },
                { it.districtNum },
                { d -> PartyResult(d.votes.entries.maxByOrNull { it.value }?.key?.party, d.leaderHasWon).asOneTimePublisher() },
                { listOf(10, 11, 12, 13, 14) },
                { listOf(9, 10, 11, 12, 13, 14) },
                { "CHARLOTTETOWN".asOneTimePublisher() }
            )
            .build("CABINET MEMBERS IN CHARLOTTETOWN".asOneTimePublisher())
        panel.setSize(1024, 512)
        compareRendering("MultiResultPanel", "MapAdditionalHighlights-1", panel)
    }

    @Test
    fun testPartiesOnly() {
        val districts = ArrayList<District>()
        districts.add(
            District(
                30,
                "Saint John East",
                mapOf(
                    Candidate("Glen Savoie", pc, true) to 3507,
                    Candidate("Phil Comeau", lib) to 1639,
                    Candidate("Gerald Irish", grn) to 394,
                    Candidate("Patrick Kemp", pa) to 434,
                    Candidate("Josh Floyd", ndp) to 248
                ),
                true,
                mapOf(
                    lib to 1775,
                    pc to 3017,
                    grn to 373,
                    ndp to 402,
                    pa to 1047
                )
            )
        )
        districts.add(
            District(
                31,
                "Portland-Simonds",
                mapOf(
                    Candidate("Trevor Holder", pc, true) to 3170,
                    Candidate("Tim Jones", lib) to 1654,
                    Candidate("Stefan Warner", grn) to 483,
                    Candidate("Darella Jackson", pa) to 282,
                    Candidate("Erik Heinze-Milne", ndp) to 164
                ),
                true,
                mapOf(
                    lib to 1703,
                    pc to 3168,
                    grn to 435,
                    ndp to 449,
                    ind to 191
                )
            )
        )
        districts.add(
            District(
                32,
                "Saint John Harbour",
                mapOf(
                    Candidate("Arlene Dunn", pc) to 2181,
                    Candidate("Alice McKim", lib) to 1207,
                    Candidate("Brent Harris", grn) to 1224,
                    Candidate("Tony Gunn", pa) to 186,
                    Candidate("Courtney Pyrke", ndp) to 309,
                    Candidate("Mike Cyr", ind) to 47,
                    Candidate("Arty Watson", ind) to 114
                ),
                false,
                mapOf(
                    lib to 1865,
                    pc to 1855,
                    grn to 721,
                    ndp to 836,
                    pa to 393
                )
            )
        )
        districts.add(
            District(
                33,
                "Saint John Lancaster",
                mapOf(
                    Candidate("Dorothy Shephard", pc, true) to 3560,
                    Candidate("Sharon Teare", lib) to 1471,
                    Candidate("Joanna Killen", grn) to 938,
                    Candidate("Paul Seelye", pa) to 394,
                    Candidate("Don Durant", ndp) to 201
                ),
                true,
                mapOf(
                    lib to 1727,
                    pc to 3001,
                    grn to 582,
                    ndp to 414,
                    pa to 922
                )
            )
        )
        val swingometerOrder = listOf(ndp, grn, lib, ind, pc, pa)
        val districtPublisher = Publisher(districts)
        val panel = ofParties(
            districtPublisher,
            { it.partyVotes },
            { ("DISTRICT " + it.districtNum).asOneTimePublisher() },
            { it.name.uppercase().asOneTimePublisher() }
        )
            .withPrev(
                { it.prevVotes.asOneTimePublisher() },
                { "SWING SINCE 2018".asOneTimePublisher() },
                compareBy { swingometerOrder.indexOf(it) }
            )
            .build("ELECTION 2020: NEW BRUNSWICK DECIDES".asOneTimePublisher())
        panel.setSize(1024, 512)
        compareRendering("MultiResultPanel", "PartiesOnly-1", panel)
        districts.add(
            District(
                34,
                "Kings Centre",
                mapOf(
                    Candidate("Bill Oliver", pc, true) to 4583,
                    Candidate("Bruce Bryer", grn) to 1006,
                    Candidate("Paul Adams", lib) to 911,
                    Candidate("William Edgett", pa) to 693,
                    Candidate("Margaret Anderson Kilfoil", ndp) to 254
                ),
                true,
                mapOf(
                    lib to 1785,
                    pc to 3267,
                    grn to 731,
                    ndp to 342,
                    pa to 1454
                )
            )
        )
        districts.add(
            District(
                35,
                "Fundy-The Isles-Saint John West",
                mapOf(
                    Candidate("Andrea Anderson-Mason", pc, true) to 4740,
                    Candidate("Tony Mann", lib) to 726,
                    Candidate("Vincent Edgett", pa) to 688,
                    Candidate("Lois Mitchell", grn) to 686,
                    Candidate("Sharon Greenlaw", ndp) to 291
                ),
                true,
                mapOf(
                    lib to 2422,
                    pc to 3808,
                    grn to 469,
                    ndp to 203,
                    pa to 1104
                )
            )
        )
        districtPublisher.submit(districts)
        compareRendering("MultiResultPanel", "PartiesOnly-2", panel)
        districts.removeAt(3)
        districts.removeAt(2)
        districts.removeAt(1)
        districts.removeAt(0)
        districtPublisher.submit(districts)
        compareRendering("MultiResultPanel", "PartiesOnly-3", panel)
    }

    private fun peiShapesByDistrict(): Map<Int, Shape> {
        val peiMap = MapFrameTest::class.java
            .classLoader
            .getResource("com/joecollins/graphics/shapefiles/pei-districts.shp")
        return readShapes(peiMap, "DIST_NO", Int::class.java)
    }

    private class District(
        val districtNum: Int,
        val name: String,
        var votes: Map<Candidate, Int>,
        var leaderHasWon: Boolean,
        val prevVotes: Map<Party, Int>,
        private var status: String = "100% REPORTING",
        private var pctReporting: Double = 1.0
    ) {
        private var topTwoToRunoff = false

        private val statusPublisher = Publisher(status)
        fun getStatus(): Flow.Publisher<String> {
            return statusPublisher
        }

        private val pctReportingPublisher = Publisher(pctReporting)
        fun getPctReporting(): Flow.Publisher<Double> {
            return pctReportingPublisher
        }

        private val votesPublisher = Publisher(votes)
        fun getVotes(): Flow.Publisher<Map<Candidate, Int>> {
            return votesPublisher
        }

        val partyVotes = Publisher(calculatePartyVotes())
        private fun calculatePartyVotes() =
            votes.entries
                .groupBy { it.key.party }
                .mapValues { e -> e.value.sumOf { it.value } }

        private val leaderHasWonPublisher = Publisher(leaderHasWon)
        fun getLeaderHasWon(): Flow.Publisher<Boolean> {
            return leaderHasWonPublisher
        }

        val winner = Publisher(calculateWinner())
        private fun calculateWinner() = if (leaderHasWon) votes.entries.maxByOrNull { it.value }!!.key else null

        val runoff = Publisher(calculateRunoff())
        private fun calculateRunoff() =
            if (!topTwoToRunoff)
                null
            else
                votes.entries.asSequence().sortedByDescending { it.value }
                    .take(2)
                    .map { it.key }
                    .toSet()

        val leader = Publisher(calculateLeader())
        private fun calculateLeader() = if (votes.values.all { it == 0 })
            null
        else
            Pair(
                votes.entries.asSequence()
                    .filter { it.value > 0 }
                    .maxByOrNull { it.value }
                !!.key,
                leaderHasWon
            )

        fun update(status: String, pctReporting: Double, votes: Map<Candidate, Int>) {
            this.status = status
            this.pctReporting = pctReporting
            this.votes = votes
            update()
        }

        fun update(
            status: String,
            pctReporting: Double,
            votes: Map<Candidate, Int>,
            leaderHasWon: Boolean
        ) {
            this.status = status
            this.pctReporting = pctReporting
            this.votes = votes
            this.leaderHasWon = leaderHasWon
            update()
        }

        fun declareRunoff() {
            topTwoToRunoff = true
            update()
        }

        private fun update() {
            statusPublisher.submit(status)
            pctReportingPublisher.submit(pctReporting)
            votesPublisher.submit(votes)
            partyVotes.submit(calculatePartyVotes())
            leaderHasWonPublisher.submit(leaderHasWon)
            winner.submit(calculateWinner())
            runoff.submit(calculateRunoff())
            leader.submit(calculateLeader())
        }
    }

    companion object {
        private val lib = Party("Liberal", "LIB", Color.RED)
        private val grn = Party("Green", "GRN", Color.GREEN.darker())
        private val pc = Party("Progressive Conservative", "PCP", Color.BLUE)
        private val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        private val pa = Party("People's Alliance", "PA", Color.MAGENTA.darker())
        private val ind = Party("Independent", "IND", Color.GRAY)
        private val dem = Party("Democratic", "DEM", Color.BLUE)
        private val gop = Party("Republican", "GOP", Color.RED)
    }
}
