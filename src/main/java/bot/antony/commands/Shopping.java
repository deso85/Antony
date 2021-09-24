package bot.antony.commands;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class Shopping implements ServerCommand {

	private TextChannel channel;

	@Override
	public void performCommand(Member m, TextChannel channel, Message message) {

		setChannel(channel);
		
		EmbedBuilder eb = new EmbedBuilder()
				.setColor(Antony.getBaseColor())
				.setTitle("Kaufempfehlungen")
				.setDescription("Da häufig nach Kaufempfehlungen gefragt wird, findest du hier eine Liste von Dingen, die man für die Ameisenhaltung und DIY-Projekte gut gebrauchen kann."
						+ "\n\nDu hast eine Empfehlung? Lass es mich wissen und ggf. füge ich sie der Liste hinzu.")
				.setFooter("Solltet ihr über die gelisteten Links etwas kaufen, erhält Chasil hierdurch eine kleine Verkaufsprovision, ihr zahlt aber keinen Cent mehr!"
						+ "\n\nAntony Version " + Antony.getVersion());
		
		eb.addField("Ausbruchschutz",
				"[Paraffinöl](https://amzn.to/3pXBlMz)"		//B01MG4WI1Z
				+ "\n[Talkum](https://amzn.to/3qYmwuD)",	//B002IJO78M
				false);
		
		eb.addField("Beheizung / Klimatisierung",
				"[Heizkabel](https://amzn.to/3q0n9SZ)"					//B003YO8WL8
				+ "\n[Heizmatte](https://amzn.to/2ZWUm78)"				//B07JYJQKNH
				+ "\n[Steckdosenthermostat](https://amzn.to/2ZWqdoK)",	//B07DC8B4SV
				false);
		
		eb.addField("DIY - Formikarienbau und mehr",
				"[Abtönfarben](https://amzn.to/3qXPoD2)"						//B08GMBNMGP
				+ "\n[Akku-Heißklebepistole](https://amzn.to/2Mss8xZ)"			//B088BNP6CV
				+ "\n[Aquariensilikon](https://amzn.to/3aVhI3u)"				//B010NQS138
				+ "\n[Bandmaß (5m)](https://amzn.to/3qZH7i5)"					//B0024QL8MC
				+ "\n[Bohrer Set](https://amzn.to/3q0HMyp)"						//B001IBMO7C
				+ "\n[Dremel](https://amzn.to/3r37R1g)"							//B01MRYZTJS
				+ "\n[Forstnerbohrer Set](https://amzn.to/3pYzAyz)"				//B072XJ5NG3
				+ "\n[Frässtifte für Dremel](https://amzn.to/3qYCwN5)"			//B075KB96S6
				+ "\n[Gaze - fein (0,125mm Mesh)](https://amzn.to/3snYjOy)"		//B07NS653BP
				+ "\n[Gaze - mittel (0,22mm Mesh)](https://amzn.to/2O43NPw)"	//B00IMJB3SO
				+ "\n[Glasbohrer (27mm)](https://amzn.to/3q6LQgN)"				//B0015NULG4
				+ "\n[Hobbymesser / Skalpell](https://amzn.to/3kEddO9)"			//B07V1MKKLB
				+ "\n[Magnete (stark)](https://amzn.to/3rbYtbL)"				//B06X977K8L
				+ "\n[Messschieber](https://amzn.to/3r0J5i2)"					//B073S69SQ1
				+ "\n[Modellgips](https://amzn.to/3qYJn9h)"						//B0012FDY6O
				+ "\n[Nagarium (67x36,5x20cm Arena)](https://amzn.to/30fMBcO)",	//B0843CY2T2
				false);
		
		eb.addField("Einrichtung und Deko",
				"[Deko Schädel](https://amzn.to/3q0dz2i)"							//B079WLWLTM
				+ "\n[Deko Treibholz (17,5x10x8cm)](https://amzn.to/3r1yVxH)"		//B0151F0RRE
				+ "\n[Futterschale (33mm, Münzkapseln)](https://amzn.to/2ZUeBCt)"	//B07C87YLP9
				+ "\n[Futterschale (Wald)](https://amzn.to/2ZXxF2A)"				//B003TOLU6C
				+ "\n[Futterschale (Wüste)](https://amzn.to/3qUUy2G)"				//B002YK1S6E
				+ "\n[Kabelverbinder / Beckenverbinder](https://amzn.to/3pYjwNo)"	//B01BHT4CBK
				+ "\n[Kokoshumus](https://amzn.to/3kqRKry)"							//B000MWQNP2
				+ "\n[Lehmpulver](https://amzn.to/3qZLbyO)"							//B007Q0BWPE
				+ "\n[Rote Folie](https://amzn.to/2ZQYzcA)"							//B07GVJTB4X
				+ "\n[Schlauch (transparent)](https://amzn.to/3kzXIqo)"				//B06Y18NDLF
				+ "\n[Thermometer](https://amzn.to/3aUVUF9)",						//B07JW7K2M5
				false);
		
		eb.addField("Futtertiere, Bodenpolizei etc.",
				"[Angelmaden (werden Stubenfliegen)](https://amzn.to/3dRzMxo)"							//B014FRUTG8
				+ "\n[Fruchtfliegen, groß](https://amzn.to/2PhtE7l)"									//B00PUJ7SCY
				+ "\n[Heimchen, mittel (ca. 700stk.)](https://amzn.to/2P7im5k)"							//B0162X2VU0
				+ "\n[Lock & Lock (350ml für wiederv. Fruchtfliegenansatz)](https://amzn.to/3ktQLXN)"	//B0000AN4CY
				+ "\n[Mehlwürmer (1kg)](https://amzn.to/3q55f1C)"										//B00PSFIE78
				+ "\n[Schokoschaben (100, groß)](https://amzn.to/2PAzCA9)"									//B012NQTSGU
				+ "\n[Sepia-Schale (für z.B. Asseln)](https://amzn.to/2ZWZHLT)"							//B00080HF3M
				+ "\n[Springschwänze](https://amzn.to/3pXE4Wl)"											//B00PUJEKNE
				+ "\n[Waldschaben (50, groß)](https://amzn.to/3v0oQ6D)"										//B00PUIKRIW
				+ "\n[Waldschaben (100, klein)](https://amzn.to/3bmTuzt)"										//B00PUI4V98
				+ "\n[Waldschaben (50, mittel)](https://amzn.to/30mt2PU)"									//B00PUII9E6
				+ "\n[Wassergel Granulat (ca. 7,5l Wasser)](https://amzn.to/3uBnOxF)",					//B083QMG84N
				false);
		
		eb.addField("Zubehör / Sonstiges",
				"[Federstahlpinzette (spitz)](https://amzn.to/3r0Ok18)"					//B0772QZTJC
				+ "\n[Federstahlpinzette (gerundet)](https://amzn.to/3pXOlSu)"			//B0772NN767
				+ "\n[Kanülen (stumpf)](https://amzn.to/37NuRJJ)"						//B078C7MQK2
				+ "\n[LED Beleuchtung](https://amzn.to/2O2iUsL)"						//B07413VYZJ
				+ "\n[Lupe mit Licht](https://amzn.to/3sBcwI5)"							//B07Q3WXLJW
				+ "\n[PVA-Saugschwamm](https://amzn.to/3uygdjs)"						//B07J55LVS6
				+ "\n[Reagenzgläser (160x16mm, Glas)](https://amzn.to/2ZVu7xW)"			//B015ZQAP26
				+ "\n[Reagenzgläser (100x16mm, Kunststoff)](https://amzn.to/3ksJO9d)"	//B015WGFEIO
				+ "\n[RG Reiniger](https://amzn.to/3r0qSkH)"							//B07SL25GF9
				+ "\n[Rohrisolierung (RG Abdunklung)](https://amzn.to/3aXCgIz)"			//B00BEPQCIY
				+ "\n[Spritzen (10ml)](https://amzn.to/3uyDotT)"						//B00PQDROC8
				+ "\n[Watte](https://amzn.to/2O5YjUk)",									//B082VP8C3J
				false);
		
		channel.sendMessageEmbeds(eb.build()).queue();
		
	}

	
	// --------------------------------------------------
	// Getter & Setter
	// --------------------------------------------------

	public TextChannel getChannel() {
		return channel;
	}

	public void setChannel(TextChannel channel) {
		this.channel = channel;
	}
}