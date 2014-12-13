package argus.reader;

import argus.util.PluginLoader;
import it.unimi.dsi.lang.MutableString;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;

/**
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class JsonReaderTest {

    @Test
    public void testWikipedia() throws Exception {
        InputStream input = getClass().getResourceAsStream("wikipedia.json");

        Reader reader = PluginLoader.getCompatibleReader("application/json").newInstance();
        MutableString text = reader.readDocumentContents(input);

        assertEquals("warnings query * Formatting of continuation data will be changing soon. To continue using the current formatting, use the 'rawcontinue' parameter. To begin using the new format, pass an empty string for 'continue' in the initial query.    query normalized from Argus_Panoptes  to Argus Panoptes    pages 1761517 pageid 1761517  ns 0  title Argus Panoptes  revisions contentformat text/x-wiki  contentmodel wikitext  * [[Image:Io Argos Staatliche Antikensammlungen 585.jpg|thumb|right|280px|[[Io (mythology)|Io]] (as cow) and Argus, black-figure [[amphora]], 540–530 BC, [[Staatliche Antikensammlung]]en (Inv. 585).]]\n" +
                        "'''Argus Panoptes''' (or '''Argos''') is the name of the 100-eyed giant in [[Greek mythology]].\n" +
                        "\n" +
                        "==Mythology==\n" +
                        "Argus Panoptes ({{lang|grc|Ἄργος Πανόπτης}}), guardian of the [[:wikt:heifer|heifer]]-[[nymph]] [[Io (mythology)|Io]] and son of [[Arestor]],<ref>Therefore called ''Arestorides'' (Pseudo-[[Apollodorus of Athens|Apollodorus]], ''[[Bibliotheca (Pseudo-Apollodorus)|Bibliotheca]]'' ii.1.3, [[Apollonius Rhodius]] i.112, [[Ovid]] ''[[Metamorphoses (poem)|Metamorphoses]]'' i.624). According to [[Pausanias (geographer)|Pausanias]] (ii.16.3), Arestor was the consort of [[Mycene]], the [[eponymous]] nymph of nearby [[Mycenae]].</ref> was a primordial [[Giant (mythology)|giant]] whose [[epithet]], \"''[[Panoptes]]''\", \"all-seeing\", led to his being described with multiple, often one hundred, eyes. The epithet ''Panoptes'' was applied to the [[Titan (mythology)|Titan]] of the Sun, [[Helios]], and was taken up as an epithet by [[Zeus]], ''Zeus Panoptes''. \"In a way,\" [[Walter Burkert]] observes, \"the power and order of [[Argos]] the city are embodied in Argos the [[Herder|neatherd]], lord of the herd and lord of the land, whose name itself is the [[Argolid|name of the land]].\"<ref>[[Walter Burkert]], ''Homo Necans'' (1972) 1983:166-67.</ref>\n" +
                        "\n" +
                        "The epithet ''Panoptes'', reflecting his mythic role, set by Hera as a very effective watchman of Io, was described in a fragment of a lost poem ''[[Aegimius (poem)|Aigimios]]'', attributed to Hesiod:<ref>Hesiodic ''[[Aegimius|Aigimios]]'', fragment 294, reproduced in Merkelbach and West 1967 and noted in Burkert 1983:167 note 28.</ref>\n" +
                        "\n" +
                        "{{quote|''And set a watcher upon her, great and strong Argos, who with four eyes looks every way. And the goddess stirred in him unwearying strength: sleep never fell upon his eyes; but he kept sure watch always.''}}\n" +
                        "\n" +
                        "In the 5th century and later, Argus' wakeful alertness was explained for an increasingly literal culture as his having so many eyes that only a few of the eyes would sleep at a time: there were always eyes still awake. In the 2nd century AD [[Pausanias (geographer)|Pausanias]] noted at Argos, in the temple of Zeus Larissaios, an archaic image of Zeus with a third eye in the center of his forehead, allegedly [[Priam]]'s ''Zeus Herkeios'' purloined from Troy.<ref>Pausanias, 2.24.3. (noted by Burkert 1983:168 note 28).</ref> According to [[Ovid]], to commemorate her faithful watchman, Hera had the hundred eyes of Argus preserved forever, in a [[peacock]]'s tail.<ref>[[Ovid]] I, 625. The [[peacock]] is an Eastern bird, unknown to Greeks before the time of Alexander.</ref>\n" +
                        "[[File:Fábula de Mercurio y Argos, by Diego Velázquez.jpg|thumb|left|Argus dozes off: [[Velázquez]] renders the theme of stealth and murder in modern dress, 1659 ([[Prado]])]]\n" +
                        "Argus was [[Hera]]'s servant. His great service to the [[Twelve Olympians|Olympian]] pantheon was to slay the [[chthonic]] [[Serpent (symbolism)|serpent]]-legged monster [[Echidna (mythology)|Echidna]] as she slept in her cave.<ref>[[Homer]], ''[[Iliad]]'' ii.783; [[Hesiod]], ''[[Theogony]]'', 295ff; Pseudo-[[Apollodorus of Athens|Apollodorus]], ''[[Bibliotheca (Pseudo-Apollodorus)|Bibliotheca]]'' ii.i.2).</ref> Hera's defining task for Argus was to guard the white heifer Io from Zeus, keeping her chained to the sacred olive tree at the [[Argive Heraion]].<ref>Pseudo-[[Apollodorus of Athens|Apollodorus]], ''[[Bibliotheke]], 2.6.</ref> She charged him to \"Tether this cow safely to an olive-tree at [[Nemea]]\". Hera knew that the heifer was in reality [[Io (mythology)|Io]], one of the many nymphs Zeus was coupling with to establish a new order. To free Io, Zeus had Argus slain by [[Hermes]]. Hermes, disguised as a shepherd, first put all of Argus's eyes asleep with spoken charms, then slew him by hitting him with a stone, the first stain of bloodshed among the new generation of gods.<ref>[[Hermes]] was tried, exonerated, and earned the epithet ''Argeiphontes'', \"killer of Argos\".</ref>\n" +
                        "\n" +
                        "The myth makes the closest connection of Argos, the neatherd, with the [[bull (mythology)|bull]]. In the  ''Library'' of pseudo-Apollodorus, \"Argos killed the bull that ravaged [[Arcadia]], ''then clothed himself in its skin.''\"<ref>Pseudo-[[Apollodorus of Athens|Apollodorus]], [[Bibliotheke]], 2.4.</ref>\n" +
                        "\n" +
                        "The sacrifice of Argos liberated Io and allowed her to wander the earth, although tormented by a [[gadfly (mythology)|gadfly]] sent by Hera.\n" +
                        "\n" +
                        "==In popular culture==\n" +
                        "* [[The Argus (Australia)|''The Argus'']] was a daily [[newspaper]] in [[Melbourne]], [[Australia]], that was published between 1846 and 1957.\n" +
                        "\n" +
                        "* Alternative rock band [[Ween]]'s eighth studio album [[Quebec (album)|Quebec]] has a song entitled \"The Argus\", which refers to the Argus' many eyes.\n" +
                        "\n" +
                        "* Argus is the title of the Wishbone Ash's third album.\n" +
                        "\n" +
                        "* Argus is featured in the ''[[Percy Jackson & the Olympians]]'' series of books as Camp Half-Blood's security guard.\n" +
                        "\n" +
                        "* The [[The Nth Degree (Star Trek: The Next Generation)|Argus Array]] was a multi-aperture space telescope in [[Star Trek]].\n" +
                        "\n" +
                        "* Argus is mentioned in the Irish poet Antoine Ó Raifteiri's poem 'An Pótaire ag Moladh an Uisce Beatha'.\n" +
                        "\n" +
                        "* [[J.K. Rowling]], author of the ''[[Harry Potter]]'' novels, gave the name [[Argus Filch]] to the caretaker of [[Hogwarts School of Witchcraft and Wizardry]].<ref>{{cite book|first=J.K.|last=Rowling|title=Harry Potter and the Philosopher's Stone|year=1997}}</ref>\n" +
                        "\n" +
                        "* The fifteenth colossus from the video game ''[[Shadow of the Colossus]]'' is called Argus and nicknamed \"The Sentinel\" and \"Vigilant Guard\". The hundreds of eyes carved into the temple that he resides in refers to the omnividence (all-seeing ability) of Argus Panoptes and the watchful colossus himself.\n" +
                        "\n" +
                        "* A once highly sought Notorious Monster from the video game ''[[Final Fantasy XI]]'' is called Argus. It has close to a dozen visible eyes and drops an accuracy enchanting necklace.\n" +
                        "\n" +
                        "* One of the monsters from ''[[Kyōryū Sentai Zyuranger]]'' and its American counterpart ''[[Mighty Morphin Power Rangers]]'' is based on Argos. It is called \"Dora Argos\" in Japanese, in ''Power Rangers'' it is called \"Eye Guy\" and is a creature composed entirely of eyeballs.\n" +
                        "\n" +
                        "* Similarly, Argus Panoptes served as the inspiration for one of the [[Kaijin]] from ''[[Kamen Rider Wizard]]'', the Phantom Argos.\n" +
                        "\n" +
                        "* Argus is the name of Jack's pet peacock on the NBC TV show ''[[30 Rock]]''. Jack believes Argus to be Don Giess' spirit watching over him.\n" +
                        "\n" +
                        "* In the mobile video game ''[[God of War: Betrayal]]'', Argos is featured as the giant pet of [[Hera]].\n" +
                        "\n" +
                        "* In the novel \"Luka and the Fire of Life\", by Salman Rushdie, Argus Panoptes is one of the five appointed guardians of the 'Fire of Life'.\n" +
                        "\n" +
                        "* Argus is the name of a Macedonian heavy metal band, formed in 1987.\n" +
                        "\n" +
                        "* Argus is the name of a fictional [[Private military corporation|PMC]] in the video games [[Splinter Cell: Pandora Tomorrow]] and [[Splinter Cell: Chaos Theory]].\n" +
                        "\n" +
                        "* In the video game ''[[Skullgirls]]'', a character named Peacock is equipped with the Argus System. It allows her to see as well as use her blockbuster, Argus Agony.\n" +
                        "\n" +
                        "* In indie game ''[[La-Mulana]]'', Argos appears as a blue giant that can only be defeated by weapon called Serpent Staff.\n" +
                        "\n" +
                        "* Argus was the name of a character created for DC Comics \"Bloodlines\" event, appearing in Flash Annual #6 and later his own limited mini-series. He was depicted as a vigilante who turned completely invisible when not in direct light, and his eyes could see every spectrum of light, including X-ray and ultraviolet.\n" +
                        "\n" +
                        "* Argus Panoptes was featured in [[Marvel Comics]]. He was revived by [[Hera (Marvel Comics)|Hera]] to be in charge of the Panopticon (a computer surveillance system that was set up to help defend New Olympus).<ref>''Incredible Hercules'' #138</ref>\n" +
                        "\n" +
                        "* A.R.G.U.S. (Advanced Research Group Uniting Superhumans) is the name of a government organization in the fictional DC Universe. The name stems from the secondary objective of the organization, which is to watch for threats should the Justice League ever fail.<ref>http://www.dccomics.com/comics/forever-evil-argus-2013/forever-evil-argus-1</ref>\n" +
                        "\n" +
                        "==Notes==\n" +
                        "{{Reflist|2}}\n" +
                        "\n" +
                        "==External links==\n" +
                        "{{commons category}}\n" +
                        "* [http://www.theoi.com/Gigante/GiganteArgosPanoptes.html Theoi Project - Gigante Argos Panoptes]\n" +
                        "* [http://warburg.sas.ac.uk/vpc/VPC_search/subcats.php?cat_1=5&cat_2=246 Warburg Institute Iconographic Database (ca 250 images of Io and Argus)]\n" +
                        "\n" +
                        "[[Category:Ancient Argos]]\n" +
                        "[[Category:Arcadian mythology]]\n" +
                        "[[Category:Mythology of Argos]]\n" +
                        "[[Category:Monsters]]\n" +
                        "[[Category:Greek giants]]\n" +
                        "[[Category:Greek legendary creatures]]       ",
                text.toString());
    }
}
