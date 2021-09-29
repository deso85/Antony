package bot.antony.commands;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class Emergency implements ServerCommand {

	private TextChannel channel;

	@Override
	public void performCommand(Member m, TextChannel channel, Message message) {

		setChannel(channel);
		
		String[] userMessage = message.getContentDisplay().split(" ");
			
		if (userMessage.length > 1) {
			EmbedBuilder eb = getEB();
			boolean longtext = false;
			
			if (userMessage.length > 2) {
				if(userMessage[2].toLowerCase().equals("long")) {
					longtext = true;
				}
			}
				
			switch (userMessage[1].toLowerCase()) {
			case "milben":
				
				eb.setTitle("Notfall - Milben", "https://ameisenwiki.de/index.php/Milben");
				//eb.setThumbnail("https://www.antwiki.org/wiki/images/9/97/Crematogaster_ferrarii_with_Myrmozercon._Hong_Kong._Fran%C3%A7ois_Brassard_%281%29.jpg");
				
				//short text with basic information
				if(!longtext) {
					
					eb.addField("Allgemeines & Disclaimer",
							"Milben sind ca. 1-2mm groß und mit ca. 40.000 beschriebenen Arten die größte Gruppe der Spinnentiere und sehr schwer korrekt zu bestimmen."
							+ "\n\nHier findet ihr Hinweise darauf, wie ihr das Schadpotential von Milben einschätzen und was ihr gegen diese machen könnt.",
							false);
					
					eb.addField("Ungefährliche Milben",
							"Die meisten Milben sind ungefährlich und können sich mit der Zeit von allein ansiedeln. **Futtermilben**"
							+ "\n- sind die häufigsten Vertreter"
							+ "\n- nutzen Ameisen als Transporter zu Futterquellen"
							+ "\n- finden sich vermehrt bei z.B. Futterresten",
							false);
					
					eb.addField("Gefährliche Milben",
							"Gefährliche Milben sind meist schon Teil einer gekauften Kolonie oder werden durch Futtertiere eingeschleppt. Sie"
							+ "\n- setzen sich vermehrt an weiche Stellen der Ameisen fest (z.B. Augen oder Gelenke)"
							+ "\n- sind seltener an Futterresten zu finden"
							+ "\n- entfernen sich normalerweise nicht von der Ameise, so lange sie lebt",
							false);
					
					eb.addField("Prävention",
							"Folgendes solltet ihr machen, um möglichst keine Milben in eure Ameisenkolonie zu bringen:"
							+ "\n- Versichert euch vor dem Kauf, dass die Ameisen milbenfrei sind und gebt diese zurück, wenn sie welche haben"
							+ "\n- **Entfernt Futterreste** regelmäßig"
							+ "\n- **Überbrüht Futtertiere** vor dem Verfüttern (tötet Milben und deren Eier)"
							+ "\n- Haltet Ameisen nicht feuchter, als es sein muss"
							+ "\n- Beheizt euer Formikarium nach Möglichkeit"
							+ "\n- Entfernt Kolonien, in denen ihr Milben seht, von anderen Kolonien",
							false);
					
					eb.addField("Bekämpfung",
							"Folgendes könnt ihr austesten, um Milben zu bekämpfen:"
							+ "\n- **Austrocknen**: Befeuchtet das Formikarium nicht weiter"
							+ "\n- Erhöht die Temperatur wenn möglich"
							+ "\n- Stresst die Ameisen nicht zu häufig"
							+ "\n- **Entfernt alle Futterreste** und füttert sparsamer"
							+ "\n- Gebt eine **Zitronenscheibe** in das Formikarium und wechselt diese regelmäßig gegen eine neue aus"
							+ "\n- **Raubmilben** (Hypoaspis miles) können z.B. online gekauft werden und fressen andere Milben"
							+ "\n\nBrecht eine Behandlung nicht zu früh ab, Milben können noch schlüpfen. Lieber ein paar Tage zu viel als zu wenig behandeln!",
							false);
					
				//long text with more information
				} else {
					eb.setDescription("Zunächst heißt es Ruhe bewahren und prüfen, ob es überhaupt ein echtes Problem gibt."
							+ "\n\nAlle Milben haben zwar einen schlechten Ruf, aber **nicht alle Milben sind schädlich für eure Ameisen**.");
					
					eb.addField("Allgemeines & Disclaimer",
							"Milben sind ca. 1-2mm groß und mit ca. 40.000 beschriebenen Arten die größte Gruppe der Spinnentiere und sehr schwer korrekt zu bestimmen."
							+ "\n\nDieser Text soll euch helfen, das Schadpotential der Milben zu bewerten und dient nicht dazu, die Tiere korrekt zu bestimmen und auf der Basis die perfekten Gegenmaßnahmen zu beschreiben."
							+ " Wir verfolgen hiermit also einen generalisierteren Ansatz."
							+ "\n\nDie meisten auftretenden Milben sind für eure Ameisen ungefährlich und das Vorhandensein dieser Tiere ist während der Haltung nicht ungewöhlich."
							+ " Je älter eine Ameisenkolonie ist, desto wahrscheinlicher ist es, dass irgendwann Milben in ihr entdeckt werden.",
							false);
					
					eb.addField("Ungefährliche Milben",
							"Die am häufigsten vorkommenden Milben sind vermutlich sogenannte ***Futtermilben***."
							+ " Diese Milben leben in der Ameisenkolonie und nutzen die Ameisen, um sich von ihnen durch das Formikarium transportieren zu lassen und zu geeigneten Futter-Quellen gebracht zu werden."
							+ " Sie kümmern sich also auch um Futterreste und können so im weitesten Sinne auch Nützlinge sein."
							+ "\n\nMit der Zeit können sich Futtermilben von allein ansiedeln, meistens gelangen diese aber z.B. über Futtertiere in das Formikarium, wo sie sich dann vermehren können."
							+ "\n\nWenn sich Milben also eher an Müll-Ecken oder Futterresten sammeln und nur verhältnismäßig wenige auf euren Ameisen sitzen, dann handelt es sich vermutlich um solche Tiere.",
							false);
					
					eb.addField("Gefährliche Milben",
							"Gefährliche Milben sind jene, die sich an Ameisen festsetzen und diese z.B. aussaugen. Wenige dieser Milben können den Ameisen zwar schaden, sie aber dadurch nicht gleich töten."
							+ " Da sich diese Milben mit der Zeit aber immer weiter vermehren und somit immer zahlreicher an den Ameisen haften, können Kolonien daran schnell zugrunde gehen."
							+ "\n\nIm Gegensatz zu Futtermilben werden verhältnismäßig wenige Milben an Futterresten zu finden sein und eher auf den Ameisen sitzen."
							+ " Häufig sitzen Schädlinge an weicheren Stellen der Ameise, wie z.B. den Augen oder Gelenken, sind aber auch an anderen Stellen zu finden."
							+ "\n\nDa es sich um schädlinge handelt, solltet ihr nicht lange warten und gleich versuchen, die Milben wieder los zu werden!",
							false);
					
					
					
					eb.addField("Prävention",
							"Auch wenn sich Milben mit der Zeit von allein im Formikarium ansiedeln können, so handelt es sich dann meistens um ungefährlichere Arten."
							+ " Gefährlichere Milben sind meist schon Teil einer gekauften Kolonie oder werden durch Futtertiere mit eingebracht. Also setzt die Prävention genau dort an."
							+ "\n\nBeim **Kauf einer Ameisenkolonie** solltet ihr euch durch den Verkäufer bestätigen lassen, dass die Kolonie frei von Milben ist oder garantieren lassen, dass etwaige Milben unschädlich sind."
							+ " Solltet ihr bei einer neuen Kolonie Milben entdecken, haltet sie nicht in der Nähe von anderen Kolonien, und haltet die neue Kolonie zunächst in Quarantäne."
							+ "\n\n**Futtertiere** sollten idealer Weise **überbrüht** werden, bevor man sie verfüttert. Dies hat den Vorteil, dass Milben oder ihre winzigen Eier abgetötet werden."
							+ " *Darüber hinaus muss das Futtertier nicht um sein Leben kämpfen und verletzt dabei dann auch keine Ameisen.*"
							+ " Milben sind vergleichsweise kälteresistent und das Eisfach oder ähnliches ist weniger effektiv.",
							false);
					
					eb.addField("Bekämpfung von Milben",
							"Milben lassen sich leider nur schwer bekämpfen und keine Methode garantiert Erfolg."
							+ "\n\n**Austrocknen** ist ein guter Anfang. Milben mögen feuchtere Habitate und lassen sich einfacher bekämpfen, wenn man das Formikarium nicht mehr befeuchtet."
							+ " Achtet aber darauf, dass eure Ameisen dann nicht noch größere Probleme haben."
							+ "\n\n**Zitronenscheiben** sollen Milben anlocken und sie dazu bewegen, von den Ameisen abzulassen und sich an der Zitronenscheibe festzusaugen. Wenn ihr also regelmäßig, z.B. täglich, die alte Zitronenscheibe entfernt und eine neue in das Formikarium legt, könnt ihr die Milben mit der Zeit reduzieren und ggf. so auch los werden."
							+ "\n\n**Raubmilben** (z.B. Hypoaspis miles) können gekauft und in das Formikarium gegeben werden. Diese Milben sind spezialisiert darauf, z.B. andere Milben zu fressen und sterben, sobald sie keine anderen Milben mehr finden."
							+ "\n\nBedenkt in jedem Fall, dass Milben oder deren Eier auch dann noch da sein können, wenn ihr sie nicht mehr seht und brecht die Behandlung nicht zu früh ab.",
							false);
				}
				
				eb.addField("Nützliche Links",
						"Video: [Medoin vs. Milben](https://www.youtube.com/watch?v=eWLORXbY5SY)"
						+ "\nVideo: [AntsCanda - Ants vs. Mites](https://www.youtube.com/watch?v=NLZxm7CuPpY) ab ca. der 2. Hälfte des Videos"
						+ "\nVideo: [Ants Kingdom Asia: Zitronenscheibe gegen Milben](https://www.youtube.com/watch?v=tLmxFvE7qeE)"
						+ "\nWebsite: [Ameisenwiki](https://ameisenwiki.de/index.php/Milben)",
						false);
				
				eb.setImage("https://www.antwiki.org/wiki/images/9/97/Crematogaster_ferrarii_with_Myrmozercon._Hong_Kong._Fran%C3%A7ois_Brassard_%281%29.jpg");
				
				getChannel().sendMessageEmbeds(eb.build()).queue();
				
				break;
				
			case "schimmel":
				
				eb.setTitle("Notfall - Schimmel", "https://de.wikipedia.org/wiki/Schimmelpilze");
				
				eb.addField("Allgemeines & Disclaimer",
						"Schimmel in der Ameisenhaltung muss kein Problem sein. "
						+ "Achte auch bei Schimmel darauf, wie deine Ameisen darauf reagieren. "
						+ "So lange sie den Schimmel ignorieren, ist er vermutlich nicht schädlich und du brauchst dir auch keine großen Sorgen machen.",
						false);
				
				eb.addField("Schimmel im Reagenzglas",
						"Junge Kolonien werden in der Regel im Reagenzglas mit einer Wasserkammer gehalten. "
						+ "Die Watte, die die Wasserkammer von den Ameisen trennt, ist häufig dreckig und dort kann schnell auch Schimmel wachsen. "
						+ "Gegen diese Art von Schimmel kannst du leider nichts machen, wächst die Kolonie aber in der Regel schnell genug, um umzuziehen, bevor der Schimmel zu einem Problem wird. "
						+ "Du kannst den Tieren aber auch frühzeitig **ein Nest oder ein neues Reagenzglas anbieten**.",
						false);
				
				eb.addField("Futterreste",
						"Futterreste können in der Haltung schnell schimmeln. "
						+ "So lange sie in der Arena liegen, kann und sollte man die **Futterreste schnell entfernen**, andernfalls können sie schnell schimmeln oder locken Milben an. "
						+ "\n\nWenn die Ameisen ihr Futter in das Nest ziehen und dort nicht komplett verwerten, können die Futterreste dort anfangen zu schimmeln. "
						+ "Da es prinzipiell schlecht ist, das Nest zu öffnen, solltet ihr das nur im allergrößten Notfall machen. "
						+ "Schimmel sollte kein solcher Notfall sein, lasst die Futterreste also im Nest. "
						+ "Im Idealfall tragen die Ameisen die Futterreste irgendwann wieder raus. "
						+ "\n\nAlternativ könnt ihr euch **Bodenpolizei besorgen**, die solche Reste verwerten. "
						+ "Versucht lieber **häufiger kleine Portionen** zu füttern, wenn ihr euch unsicher seid, wie viel eure Ameisen annehmen. "
						+ "Dadurch senkt ihr das Risiko, dass Futterreste übrig bleiben und beginnen zu schimmeln.",
						false);
				
				eb.addField("Schimmel in der Arena",
						"Aus der Natur entnommene Dekoration und Bodengrund neigt dazu schnell zu schimmeln. "
						+ "Um unter anderem dem vorzubeugen kannst du die gesammelten Dinge **abkochen oder im Ofen/der Mikrowelle vorbehandeln**, bevor du sie in die Arena gibst. "
						+ "\n\nGenenerell schimmelt es in der Arena schneller, wenn sie feucht ist. "
						+ "Sollte es nicht notwendig sein, kannst du die **Arena** also **trocken lassen** und damit das Risiko minimieren. "
						+ "\n\nBei z.B. Gips kann es möglich sein, dass dieser schimmelt. "
						+ "Es könnte hilfreich sein, **Teile des Gips (oder ähnliches Material)** großzügig zu entfernen, damit sich der Schimmel nicht ausbreitet.",
						false);
				
				eb.addField("Nützliche Links",
						"Kauf: [Springschwänze (Bodenpolizei)](https://amzn.to/3D35HEt)",
						false);
				
				getChannel().sendMessageEmbeds(eb.build()).queue();
				
				break;
			default:
				printHelp();
				break;
			}
		} else {
			printHelp();
		}
			
		
	}


	private void printHelp() {
		//TODO: Help ausformulieren
		getChannel().sendMessage("Benutzung: " + Antony.getCmdPrefix() + "emergency (milben | schimmel)").queue();
	}
	
	private EmbedBuilder getEB() {
		
		
		EmbedBuilder eb = new EmbedBuilder()
				/*.setAuthor(ebHeadline.toString())
				.setDescription(sbStatus.toString())
				.setThumbnail(getMember().getUser().getEffectiveAvatarUrl())
				.addField("Discord beigetreten", getMember().getTimeCreated().format(formatter) + "\n(Vor "
						+ getFormattedPeriod(getMember().getTimeCreated(), formatter) + ")",
						false)
				.addField("Rollen", sbRoles.toString(), false)
				.setFooter("Member #" + (getMemberList().indexOf(getMember())+1) + " | User ID: " + getMember().getId());*/
				.setColor(Antony.getBaseColor())
				.setFooter("Antony Version " + Antony.getVersion());
				
		return eb;

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