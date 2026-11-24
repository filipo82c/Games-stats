import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.*;
import java.util.*;

/** Genera un PDF de patch notes ficticias para la demo de GameStats AI. */
public class GenDemoPdf {
    public static void main(String[] a) throws Exception {
        String[][] pages = {
            {"NEON STRIKE - NOTAS DEL PARCHE 7.4",
             "Fecha de lanzamiento: 18 de noviembre de 2026",
             "",
             "RESUMEN GENERAL",
             "El parche 7.4 se centra en equilibrar a los agentes de duelo, que dominaban",
             "las partidas clasificatorias con una tasa de victoria superior al 54%.",
             "Tambien se introduce el nuevo mapa 'Puerto Kaida' y el rifle 'Vortex-9'.",
             "",
             "CAMBIOS EN AGENTES - NERFS",
             "",
             "Kaze (Duelista)",
             "- Habilidad 'Paso Fantasma': enfriamiento aumentado de 8 a 12 segundos.",
             "- Definitiva 'Tormenta de Cuchillas': dano por cuchilla reducido de 55 a 48.",
             "- Motivo: Kaze tenia una tasa de seleccion del 41% en rango Diamante.",
             "",
             "Rhea (Duelista)",
             "- Habilidad 'Llamarada': duracion del cegado reducida de 2.0 a 1.5 segundos.",
             "- Coste de la habilidad aumentado de 200 a 250 creditos.",
             "",
             "Vex (Iniciador)",
             "- Radio del dron explorador reducido de 30 a 25 metros."},
            {"CAMBIOS EN AGENTES - BUFFS",
             "",
             "Orin (Centinela)",
             "- Torreta automatica: vida aumentada de 125 a 160 puntos.",
             "- La torreta ahora se puede recoger y recolocar una vez por ronda.",
             "- Motivo: Orin tenia la tasa de victoria mas baja del juego (44%).",
             "",
             "Mara (Controladora)",
             "- Humo 'Velo Nocturno': duracion aumentada de 14 a 17 segundos.",
             "- Cargas maximas aumentadas de 2 a 3.",
             "",
             "Tobi (Sanador)",
             "- Curacion de 'Pulso Vital' aumentada de 60 a 80 puntos de vida.",
             "",
             "CAMBIOS EN ARMAS",
             "",
             "Rifle Phantom-X: dano a la cabeza reducido de 156 a 140.",
             "Escopeta Brute: precio reducido de 1850 a 1600 creditos.",
             "Pistola Sheriff: cadencia de disparo aumentada de 4.0 a 4.4 balas por segundo.",
             "Francotirador Longbow: sin cambios en este parche."},
            {"NUEVO CONTENIDO",
             "",
             "Nuevo rifle: Vortex-9",
             "- Precio: 2700 creditos.",
             "- Dano: 38 al cuerpo, 152 a la cabeza.",
             "- Cadencia: 10 balas por segundo, cargador de 30 balas.",
             "- Es el arma con mayor dano a la cabeza entre los rifles automaticos.",
             "",
             "Nuevo mapa: Puerto Kaida",
             "- Ambientado en un puerto industrial japones durante una tormenta.",
             "- Tiene 3 zonas de plantado de bomba (A, B y C).",
             "- Incluye puertas de contenedor que se abren cada 40 segundos.",
             "",
             "CORRECCION DE ERRORES",
             "- Se corrigio un error que permitia a Kaze atravesar paredes delgadas.",
             "- Se corrigio el sonido de pasos inexistente en la zona B de 'Ciudad Ceniza'.",
             "",
             "PROXIMO PARCHE",
             "El parche 7.5 esta previsto para el 9 de diciembre de 2026 e incluira",
             "un nuevo agente controlador llamado 'Nyx'."}
        };
        try (PDDocument doc = new PDDocument()) {
            PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font reg = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            for (String[] lines : pages) {
                PDPage page = new PDPage();
                doc.addPage(page);
                try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                    float y = 740;
                    for (String l : lines) {
                        boolean title = !l.isEmpty() && l.equals(l.toUpperCase()) && !l.startsWith("-");
                        cs.beginText();
                        cs.setFont(title ? bold : reg, title ? 14 : 11);
                        cs.newLineAtOffset(60, y);
                        cs.showText(l);
                        cs.endText();
                        y -= title ? 22 : 16;
                    }
                }
            }
            doc.save(a[0]);
        }
        System.out.println("PDF creado: " + a[0]);
    }
}
