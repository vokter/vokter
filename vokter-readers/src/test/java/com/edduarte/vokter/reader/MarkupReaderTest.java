/*
 * Copyright 2014 Ed Duarte
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.edduarte.vokter.reader;

import it.unimi.dsi.lang.MutableString;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 2.0.0
 * @since 1.0.0
 */
public class MarkupReaderTest {

    @Test
    public void testWikipediaHtml() throws Exception {
        InputStream input = getClass().getResourceAsStream("wikipedia.html");

        Reader reader = new MarkupReader();
        MutableString text = reader.readDocumentContents(input);

        assertEquals("Argus Panoptes - Wikipedia, the free encyclopedia   Argus Panoptes\n" +
                "\n" +
                "From Wikipedia, the free encyclopedia  Jump to: navigation , search     \n" +
                " Io  (as cow) and Argus, black-figure amphora , 540–530 BC, Staatliche \n" +
                "Antikensammlungen  (Inv. 585). Argus Panoptes (or Argos) is the name of the \n" +
                "100-eyed giant inGreek mythology .\n" +
                "\n" +
                "\n" +
                "\n" +
                "Contents\n" +
                "\n" +
                "\n" +
                " * 1 Mythology  \n" +
                " * 2 In popular culture  \n" +
                " * 3 Notes  \n" +
                " * 4 External links  \n" +
                "\n" +
                "Mythology[edit ]\n" +
                "\n" +
                "Argus Panoptes (Ἄργος Πανόπτης), guardian of the heifer -nymph  Io  and \n" +
                "son ofArestor ,[1]  was a primordial giant  whose epithet , \"Panoptes \n" +
                "\", \"all-seeing\", led to his being described with multiple, often one hundred, \n" +
                "eyes. The epithetPanoptes was applied to the Titan  of the Sun, Helios , \n" +
                "and was taken up as an epithet byZeus , Zeus Panoptes. \"In a way,\" Walter \n" +
                "Burkert  observes, \"the power and order of Argos  the city are embodied in \n" +
                "Argos theneatherd , lord of the herd and lord of the land, whose name itself \n" +
                "is thename of the land .\"[2] \n" +
                "\n" +
                "The epithet Panoptes, reflecting his mythic role, set by Hera as a very \n" +
                "effective watchman of Io, was described in a fragment of a lost poemAigimios \n" +
                ", attributed to Hesiod:[3] \n" +
                "\n" +
                "And set a watcher upon her, great and strong Argos, who with four eyes looks \n" +
                "every way. And the goddess stirred in him unwearying strength: sleep never fell \n" +
                "upon his eyes; but he kept sure watch always.\n" +
                "\n" +
                "In the 5th century and later, Argus' wakeful alertness was explained for an \n" +
                "increasingly literal culture as his having so many eyes that only a few of the \n" +
                "eyes would sleep at a time: there were always eyes still awake. In the 2nd \n" +
                "century ADPausanias  noted at Argos, in the temple of Zeus Larissaios, an \n" +
                "archaic image of Zeus with a third eye in the center of his forehead, allegedly\n" +
                "Priam 's Zeus Herkeios purloined from Troy.[4]  According to Ovid , to \n" +
                "commemorate her faithful watchman, Hera had the hundred eyes of Argus preserved \n" +
                "forever, in apeacock 's tail.[5] \n" +
                "\n" +
                "    Argus dozes off: Velázquez  renders the theme of stealth and murder \n" +
                "in modern dress, 1659 (Prado ) Argus was Hera 's servant. His great service \n" +
                "to theOlympian  pantheon was to slay the chthonic  serpent -legged monster\n" +
                "Echidna  as she slept in her cave.[6]  Hera's defining task for Argus was \n" +
                "to guard the white heifer Io from Zeus, keeping her chained to the sacred olive \n" +
                "tree at theArgive Heraion .[7]  She charged him to \"Tether this cow safely \n" +
                "to an olive-tree atNemea \". Hera knew that the heifer was in reality Io , \n" +
                "one of the many nymphs Zeus was coupling with to establish a new order. To free \n" +
                "Io, Zeus had Argus slain byHermes . Hermes, disguised as a shepherd, first \n" +
                "put all of Argus's eyes asleep with spoken charms, then slew him by hitting him \n" +
                "with a stone, the first stain of bloodshed among the new generation of gods.[8] \n" +
                "\n" +
                "\n" +
                "The myth makes the closest connection of Argos, the neatherd, with the bull \n" +
                ". In theLibrary of pseudo-Apollodorus, \"Argos killed the bull that ravaged \n" +
                "Arcadia , then clothed himself in its skin.\"[9] \n" +
                "\n" +
                "The sacrifice of Argos liberated Io and allowed her to wander the earth, \n" +
                "although tormented by agadfly  sent by Hera.\n" +
                "\n" +
                "In popular culture[edit ]\n" +
                "\n" +
                "\n" +
                " * The Argus  was a daily newspaper  in Melbourne , Australia , that \n" +
                "was published between 1846 and 1957. \n" +
                " * Alternative rock band Ween 's eighth studio album Quebec  has a song \n" +
                "entitled \"The Argus\", which refers to the Argus' many eyes. \n" +
                " * Argus is the title of the Wishbone Ash's third album. \n" +
                " * Argus is featured in the Percy Jackson & the Olympians  series of books \n" +
                "as Camp Half-Blood's security guard. \n" +
                " * The Argus Array  was a multi-aperture space telescope in Star Trek . \n" +
                " * Argus is mentioned in the Irish poet Antoine Ó Raifteiri's poem 'An Pótaire \n" +
                "ag Moladh an Uisce Beatha'. \n" +
                " * J.K. Rowling , author of the Harry Potter  novels, gave the name Argus \n" +
                "Filch  to the caretaker of Hogwarts School of Witchcraft and Wizardry .[10] \n" +
                " \n" +
                " * The fifteenth colossus from the video game Shadow of the Colossus  is \n" +
                "called Argus and nicknamed \"The Sentinel\" and \"Vigilant Guard\". The hundreds of \n" +
                "eyes carved into the temple that he resides in refers to the omnividence \n" +
                "(all-seeing ability) of Argus Panoptes and the watchful colossus himself. \n" +
                " * A once highly sought Notorious Monster from the video game Final Fantasy XI \n" +
                " is called Argus. It has close to a dozen visible eyes and drops an accuracy \n" +
                "enchanting necklace. \n" +
                " * One of the monsters from Kyōryū Sentai Zyuranger  and its American \n" +
                "counterpartMighty Morphin Power Rangers  is based on Argos. It is called \n" +
                "\"Dora Argos\" in Japanese, inPower Rangers it is called \"Eye Guy\" and is a \n" +
                "creature composed entirely of eyeballs. \n" +
                " * Similarly, Argus Panoptes served as the inspiration for one of the Kaijin \n" +
                " fromKamen Rider Wizard , the Phantom Argos. \n" +
                " * Argus is the name of Jack's pet peacock on the NBC TV show 30 Rock . Jack \n" +
                "believes Argus to be Don Giess' spirit watching over him. \n" +
                " * In the mobile video game God of War: Betrayal , Argos is featured as the \n" +
                "giant pet ofHera . \n" +
                " * In the novel \"Luka and the Fire of Life\", by Salman Rushdie, Argus Panoptes \n" +
                "is one of the five appointed guardians of the 'Fire of Life'. \n" +
                " * Argus is the name of a Macedonian heavy metal band, formed in 1987. \n" +
                " * Argus is the name of a fictional PMC  in the video games Splinter Cell: \n" +
                "Pandora Tomorrow  and Splinter Cell: Chaos Theory . \n" +
                " * In the video game Skullgirls , a character named Peacock is equipped with \n" +
                "the Argus System. It allows her to see as well as use her blockbuster, Argus \n" +
                "Agony. \n" +
                " * In indie game La-Mulana , Argos appears as a blue giant that can only be \n" +
                "defeated by weapon called Serpent Staff. \n" +
                " * Argus was the name of a character created for DC Comics \"Bloodlines\" event, \n" +
                "appearing in Flash Annual #6 and later his own limited mini-series. He was \n" +
                "depicted as a vigilante who turned completely invisible when not in direct \n" +
                "light, and his eyes could see every spectrum of light, including X-ray and \n" +
                "ultraviolet. \n" +
                " * Argus Panoptes was featured in Marvel Comics . He was revived by Hera  \n" +
                "to be in charge of the Panopticon (a computer surveillance system that was set \n" +
                "up to help defend New Olympus).[11]  \n" +
                " * A.R.G.U.S. (Advanced Research Group Uniting Superhumans) is the name of a \n" +
                "government organization in the fictional DC Universe. The name stems from the \n" +
                "secondary objective of the organization, which is to watch for threats should \n" +
                "the Justice League ever fail.[12]  Notes[edit ]\n" +
                "\n" +
                "\n" +
                " * ^  Therefore called Arestorides (Pseudo-Apollodorus , Bibliotheca  \n" +
                "ii.1.3,Apollonius Rhodius  i.112, Ovid  Metamorphoses  i.624). According \n" +
                "toPausanias  (ii.16.3), Arestor was the consort of Mycene , the eponymous \n" +
                " nymph of nearbyMycenae . \n" +
                " * ^  Walter Burkert , Homo Necans (1972) 1983:166-67. \n" +
                " * ^  Hesiodic Aigimios , fragment 294, reproduced in Merkelbach and West \n" +
                "1967 and noted in Burkert 1983:167 note 28. \n" +
                " * ^  Pausanias, 2.24.3. (noted by Burkert 1983:168 note 28). \n" +
                " * ^  Ovid  I, 625. The peacock  is an Eastern bird, unknown to Greeks \n" +
                "before the time of Alexander. \n" +
                " * ^  Homer , Iliad  ii.783; Hesiod , Theogony , 295ff; Pseudo-\n" +
                "Apollodorus , Bibliotheca  ii.i.2). \n" +
                " * ^  Pseudo-Apollodorus , Bibliotheke , 2.6. \n" +
                " * ^  Hermes  was tried, exonerated, and earned the epithet Argeiphontes, \n" +
                "\"killer of Argos\". \n" +
                " * ^  Pseudo-Apollodorus , Bibliotheke , 2.4. \n" +
                " * ^  Rowling, J.K. (1997). Harry Potter and the Philosopher's Stone.  \n" +
                " * ^  Incredible Hercules #138 \n" +
                " * ^  \n" +
                "http://www.dccomics.com/comics/forever-evil-vokter-2013/forever-evil-vokter-1 \n" +
                " \n" +
                "External links[edit ]\n" +
                "\n" +
                "Wikimedia Commons has media related to Argus Panoptes . \n" +
                " * Theoi Project - Gigante Argos Panoptes \n" +
                " \n" +
                " * Warburg Institute Iconographic Database (ca 250 images of Io and Argus) \n" +
                "  \n" +
                "Retrieved from \"\n" +
                "http://en.wikipedia.org/w/index.php?title=Argus_Panoptes&oldid=637267009 \n" +
                "\" \n" +
                "Categories : \n" +
                " * Ancient Argos \n" +
                " * Arcadian mythology \n" +
                " * Mythology of Argos \n" +
                " * Monsters \n" +
                " * Greek giants \n" +
                " * Greek legendary creatures Hidden categories: \n" +
                " * Articles containing Ancient Greek-language text \n" +
                " * Commons category template with no category set \n" +
                " * Commons category with page title same as on Wikidata  Navigation menu\n" +
                "\n" +
                "Personal tools\n" +
                "\n" +
                "\n" +
                " * Create account \n" +
                " * Log in  Namespaces\n" +
                "\n" +
                "\n" +
                " * Article  \n" +
                " * Talk  Variants \n" +
                "\n" +
                "Views\n" +
                "\n" +
                "\n" +
                " * Read  \n" +
                " * Edit  \n" +
                " * View history  More \n" +
                "\n" +
                "Search \n" +
                "\n" +
                "  Navigation\n" +
                "\n" +
                "\n" +
                " * Main page  \n" +
                " * Contents  \n" +
                " * Featured content  \n" +
                " * Current events  \n" +
                " * Random article  \n" +
                " * Donate to Wikipedia \n" +
                "\n" +
                " * Wikimedia Shop  Interaction\n" +
                "\n" +
                "\n" +
                " * Help  \n" +
                " * About Wikipedia  \n" +
                " * Community portal  \n" +
                " * Recent changes  \n" +
                " * Contact page  Tools\n" +
                "\n" +
                "\n" +
                " * What links here  \n" +
                " * Related changes  \n" +
                " * Upload file  \n" +
                " * Special pages  \n" +
                " * Permanent link  \n" +
                " * Page information  \n" +
                " * Wikidata item  \n" +
                " * Cite this page  Print/export\n" +
                "\n" +
                "\n" +
                " * Create a book  \n" +
                " * Download as PDF  \n" +
                " * Printable version  Languages\n" +
                "\n" +
                "\n" +
                " * العربية  \n" +
                " * Беларуская  \n" +
                " * Български  \n" +
                " * Brezhoneg  \n" +
                " * Català  \n" +
                " * Čeština  \n" +
                " * Dansk  \n" +
                " * Deutsch  \n" +
                " * Ελληνικά  \n" +
                " * Español  \n" +
                " * Esperanto  \n" +
                " * Euskara  \n" +
                " * فارسی  \n" +
                " * Français  \n" +
                " * Հայերեն  \n" +
                " * Hrvatski  \n" +
                " * Bahasa Indonesia  \n" +
                " * Italiano  \n" +
                " * עברית  \n" +
                " * Қазақша  \n" +
                " * Lëtzebuergesch  \n" +
                " * Lietuvių  \n" +
                " * Magyar  \n" +
                " * Nederlands  \n" +
                " * 日本語  \n" +
                " * Norsk bokmål  \n" +
                " * Norsk nynorsk  \n" +
                " * Polski  \n" +
                " * Português  \n" +
                " * Română  \n" +
                " * Русский  \n" +
                " * Slovenčina  \n" +
                " * Slovenščina  \n" +
                " * Српски / srpski  \n" +
                " * Srpskohrvatski / српскохрватски  \n" +
                " * Suomi  \n" +
                " * Svenska  \n" +
                " * Türkçe  \n" +
                " * Українська  \n" +
                " * 中文  \n" +
                " *   Edit links  \n" +
                " *  This page was last modified on 9 December 2014 at 03:17.\n" +
                "\n" +
                " * Text is available under the Creative Commons Attribution-ShareAlike License \n" +
                " ; additional terms may apply. By using this site, you agree to the Terms \n" +
                "of Use  and Privacy Policy . Wikipedia® is a registered trademark of the \n" +
                "Wikimedia Foundation, Inc. , a non-profit organization. \n" +
                " * Privacy policy  \n" +
                " * About Wikipedia  \n" +
                " * Disclaimers  \n" +
                " * Contact Wikipedia  \n" +
                " * Developers \n" +
                " \n" +
                " * Mobile view  \n" +
                " *   \n" +
                " *   ", text.toString());
    }

    @Test
    public void testWikipediaXml() throws Exception {
        InputStream input = getClass().getResourceAsStream("wikipedia.xml");

        Reader reader = new MarkupReader();
        MutableString text = reader.readDocumentContents(input);

        assertEquals("Wikipedia enwiki http://en.wikipedia.org/wiki/Main_Page MediaWiki 1.25wmf11 \n" +
                        "first-letter Media Special Talk User User talk Wikipedia Wikipedia talk File \n" +
                        "File talk MediaWiki MediaWiki talk Template Template talk Help Help talk \n" +
                        "Category Category talk Portal Portal talk Book Book talk Draft Draft talk \n" +
                        "Education Program Education Program talk TimedText TimedText talk Module Module \n" +
                        "talk Topic Argus Panoptes 0 1761517 637267009 632931498 2014-12-09T03:17:48Z \n" +
                        "176.227.144.238 wikitext text/x-wiki [[Image:Io Argos Staatliche \n" +
                        "Antikensammlungen 585.jpg|thumb|right|280px|[[Io (mythology)|Io]] (as cow) and \n" +
                        "Argus, black-figure [[amphora]], 540–530 BC, [[Staatliche Antikensammlung]]en \n" +
                        "(Inv. 585).]] '''Argus Panoptes''' (or '''Argos''') is the name of the 100-eyed \n" +
                        "giant in [[Greek mythology]]. ==Mythology== Argus Panoptes ({{lang|grc|Ἄργος \n" +
                        "Πανόπτης}}), guardian of the [[:wikt:heifer|heifer]]-[[nymph]] [[Io \n" +
                        "(mythology)|Io]] and son of [[Arestor]],Therefore called ''Arestorides'' \n" +
                        "(Pseudo-[[Apollodorus of Athens|Apollodorus]], ''[[Bibliotheca \n" +
                        "(Pseudo-Apollodorus)|Bibliotheca]]'' ii.1.3, [[Apollonius Rhodius]] i.112, \n" +
                        "[[Ovid]] ''[[Metamorphoses (poem)|Metamorphoses]]'' i.624). According to \n" +
                        "[[Pausanias (geographer)|Pausanias]] (ii.16.3), Arestor was the consort of \n" +
                        "[[Mycene]], the [[eponymous]] nymph of nearby [[Mycenae]]. was a \n" +
                        "primordial [[Giant (mythology)|giant]] whose [[epithet]], \"''[[Panoptes]]''\", \n" +
                        "\"all-seeing\", led to his being described with multiple, often one hundred, \n" +
                        "eyes. The epithet ''Panoptes'' was applied to the [[Titan (mythology)|Titan]] \n" +
                        "of the Sun, [[Helios]], and was taken up as an epithet by [[Zeus]], ''Zeus \n" +
                        "Panoptes''. \"In a way,\" [[Walter Burkert]] observes, \"the power and order of \n" +
                        "[[Argos]] the city are embodied in Argos the [[Herder|neatherd]], lord of the \n" +
                        "herd and lord of the land, whose name itself is the [[Argolid|name of the \n" +
                        "land]].\"[[Walter Burkert]], ''Homo Necans'' (1972) 1983:166-67. The \n" +
                        "epithet ''Panoptes'', reflecting his mythic role, set by Hera as a very \n" +
                        "effective watchman of Io, was described in a fragment of a lost poem \n" +
                        "''[[Aegimius (poem)|Aigimios]]'', attributed to Hesiod:Hesiodic \n" +
                        "''[[Aegimius|Aigimios]]'', fragment 294, reproduced in Merkelbach and West 1967 \n" +
                        "and noted in Burkert 1983:167 note 28. {{quote|''And set a watcher upon \n" +
                        "her, great and strong Argos, who with four eyes looks every way. And the \n" +
                        "goddess stirred in him unwearying strength: sleep never fell upon his eyes; but \n" +
                        "he kept sure watch always.''}} In the 5th century and later, Argus' wakeful \n" +
                        "alertness was explained for an increasingly literal culture as his having so \n" +
                        "many eyes that only a few of the eyes would sleep at a time: there were always \n" +
                        "eyes still awake. In the 2nd century AD [[Pausanias (geographer)|Pausanias]] \n" +
                        "noted at Argos, in the temple of Zeus Larissaios, an archaic image of Zeus with \n" +
                        "a third eye in the center of his forehead, allegedly [[Priam]]'s ''Zeus \n" +
                        "Herkeios'' purloined from Troy.Pausanias, 2.24.3. (noted by Burkert \n" +
                        "1983:168 note 28). According to [[Ovid]], to commemorate her faithful \n" +
                        "watchman, Hera had the hundred eyes of Argus preserved forever, in a \n" +
                        "[[peacock]]'s tail.[[Ovid]] I, 625. The [[peacock]] is an Eastern bird, \n" +
                        "unknown to Greeks before the time of Alexander. [[File:Fábula de Mercurio \n" +
                        "y Argos, by Diego Velázquez.jpg|thumb|left|Argus dozes off: [[Velázquez]] \n" +
                        "renders the theme of stealth and murder in modern dress, 1659 ([[Prado]])]] \n" +
                        "Argus was [[Hera]]'s servant. His great service to the [[Twelve \n" +
                        "Olympians|Olympian]] pantheon was to slay the [[chthonic]] [[Serpent \n" +
                        "(symbolism)|serpent]]-legged monster [[Echidna (mythology)|Echidna]] as she \n" +
                        "slept in her cave.[[Homer]], ''[[Iliad]]'' ii.783; [[Hesiod]], \n" +
                        "''[[Theogony]]'', 295ff; Pseudo-[[Apollodorus of Athens|Apollodorus]], \n" +
                        "''[[Bibliotheca (Pseudo-Apollodorus)|Bibliotheca]]'' ii.i.2). Hera's \n" +
                        "defining task for Argus was to guard the white heifer Io from Zeus, keeping her \n" +
                        "chained to the sacred olive tree at the [[Argive \n" +
                        "Heraion]].Pseudo-[[Apollodorus of Athens|Apollodorus]], ''[[Bibliotheke]], \n" +
                        "2.6. She charged him to \"Tether this cow safely to an olive-tree at \n" +
                        "[[Nemea]]\". Hera knew that the heifer was in reality [[Io (mythology)|Io]], one \n" +
                        "of the many nymphs Zeus was coupling with to establish a new order. To free Io, \n" +
                        "Zeus had Argus slain by [[Hermes]]. Hermes, disguised as a shepherd, first put \n" +
                        "all of Argus's eyes asleep with spoken charms, then slew him by hitting him \n" +
                        "with a stone, the first stain of bloodshed among the new generation of \n" +
                        "gods.[[Hermes]] was tried, exonerated, and earned the epithet \n" +
                        "''Argeiphontes'', \"killer of Argos\". The myth makes the closest \n" +
                        "connection of Argos, the neatherd, with the [[bull (mythology)|bull]]. In the \n" +
                        "''Library'' of pseudo-Apollodorus, \"Argos killed the bull that ravaged \n" +
                        "[[Arcadia]], ''then clothed himself in its skin.''\"Pseudo-[[Apollodorus of \n" +
                        "Athens|Apollodorus]], [[Bibliotheke]], 2.4. The sacrifice of Argos \n" +
                        "liberated Io and allowed her to wander the earth, although tormented by a \n" +
                        "[[gadfly (mythology)|gadfly]] sent by Hera. ==In popular culture== * [[The \n" +
                        "Argus (Australia)|''The Argus'']] was a daily [[newspaper]] in [[Melbourne]], \n" +
                        "[[Australia]], that was published between 1846 and 1957. * Alternative rock \n" +
                        "band [[Ween]]'s eighth studio album [[Quebec (album)|Quebec]] has a song \n" +
                        "entitled \"The Argus\", which refers to the Argus' many eyes. * Argus is the \n" +
                        "title of the Wishbone Ash's third album. * Argus is featured in the ''[[Percy \n" +
                        "Jackson & the Olympians]]'' series of books as Camp Half-Blood's security \n" +
                        "guard. * The [[The Nth Degree (Star Trek: The Next Generation)|Argus Array]] \n" +
                        "was a multi-aperture space telescope in [[Star Trek]]. * Argus is mentioned in \n" +
                        "the Irish poet Antoine Ó Raifteiri's poem 'An Pótaire ag Moladh an Uisce \n" +
                        "Beatha'. * [[J.K. Rowling]], author of the ''[[Harry Potter]]'' novels, gave \n" +
                        "the name [[Argus Filch]] to the caretaker of [[Hogwarts School of Witchcraft \n" +
                        "and Wizardry]].{{cite book|first=J.K.|last=Rowling|title=Harry Potter and \n" +
                        "the Philosopher's Stone|year=1997}} * The fifteenth colossus from the \n" +
                        "video game ''[[Shadow of the Colossus]]'' is called Argus and nicknamed \"The \n" +
                        "Sentinel\" and \"Vigilant Guard\". The hundreds of eyes carved into the temple \n" +
                        "that he resides in refers to the omnividence (all-seeing ability) of Argus \n" +
                        "Panoptes and the watchful colossus himself. * A once highly sought Notorious \n" +
                        "Monster from the video game ''[[Final Fantasy XI]]'' is called Argus. It has \n" +
                        "close to a dozen visible eyes and drops an accuracy enchanting necklace. * One \n" +
                        "of the monsters from ''[[Kyōryū Sentai Zyuranger]]'' and its American \n" +
                        "counterpart ''[[Mighty Morphin Power Rangers]]'' is based on Argos. It is \n" +
                        "called \"Dora Argos\" in Japanese, in ''Power Rangers'' it is called \"Eye Guy\" \n" +
                        "and is a creature composed entirely of eyeballs. * Similarly, Argus Panoptes \n" +
                        "served as the inspiration for one of the [[Kaijin]] from ''[[Kamen Rider \n" +
                        "Wizard]]'', the Phantom Argos. * Argus is the name of Jack's pet peacock on the \n" +
                        "NBC TV show ''[[30 Rock]]''. Jack believes Argus to be Don Giess' spirit \n" +
                        "watching over him. * In the mobile video game ''[[God of War: Betrayal]]'', \n" +
                        "Argos is featured as the giant pet of [[Hera]]. * In the novel \"Luka and the \n" +
                        "Fire of Life\", by Salman Rushdie, Argus Panoptes is one of the five appointed \n" +
                        "guardians of the 'Fire of Life'. * Argus is the name of a Macedonian heavy \n" +
                        "metal band, formed in 1987. * Argus is the name of a fictional [[Private \n" +
                        "military corporation|PMC]] in the video games [[Splinter Cell: Pandora \n" +
                        "Tomorrow]] and [[Splinter Cell: Chaos Theory]]. * In the video game \n" +
                        "''[[Skullgirls]]'', a character named Peacock is equipped with the Argus \n" +
                        "System. It allows her to see as well as use her blockbuster, Argus Agony. * In \n" +
                        "indie game ''[[La-Mulana]]'', Argos appears as a blue giant that can only be \n" +
                        "defeated by weapon called Serpent Staff. * Argus was the name of a character \n" +
                        "created for DC Comics \"Bloodlines\" event, appearing in Flash Annual #6 and \n" +
                        "later his own limited mini-series. He was depicted as a vigilante who turned \n" +
                        "completely invisible when not in direct light, and his eyes could see every \n" +
                        "spectrum of light, including X-ray and ultraviolet. * Argus Panoptes was \n" +
                        "featured in [[Marvel Comics]]. He was revived by [[Hera (Marvel Comics)|Hera]] \n" +
                        "to be in charge of the Panopticon (a computer surveillance system that was set \n" +
                        "up to help defend New Olympus).''Incredible Hercules'' #138 * \n" +
                        "A.R.G.U.S. (Advanced Research Group Uniting Superhumans) is the name of a \n" +
                        "government organization in the fictional DC Universe. The name stems from the \n" +
                        "secondary objective of the organization, which is to watch for threats should \n" +
                        "the Justice League ever \n" +
                        "fail.http://www.dccomics.com/comics/forever-evil-vokter-2013/forever-evil-vokter-1 \n" +
                        "==Notes== {{Reflist|2}} ==External links== {{commons category}} * \n" +
                        "[http://www.theoi.com/Gigante/GiganteArgosPanoptes.html Theoi Project - Gigante \n" +
                        "Argos Panoptes] * \n" +
                        "[http://warburg.sas.ac.uk/vpc/VPC_search/subcats.php?cat_1=5&cat_2=246 Warburg \n" +
                        "Institute Iconographic Database (ca 250 images of Io and Argus)] \n" +
                        "[[Category:Ancient Argos]] [[Category:Arcadian mythology]] [[Category:Mythology \n" +
                        "of Argos]] [[Category:Monsters]] [[Category:Greek giants]] [[Category:Greek \n" +
                        "legendary creatures]] 40f8de1wi3qunrainkyxfcjn8scpk8f ",
                text.toString());
    }
}
