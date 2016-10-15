import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Class which represents the mappings creator from a given ontology file.
 * <p>
 * This class will create three mapping files:
 * - entity mappings
 * - relation mappings
 * - triples represented by their own mapping identifier
 * </p>
 */
public class TripleExporter {

    /**
     * Reads the RDF ontology file and exports the mappings between URIs and identifiers.
     *
     * @param RDFontologyFile  RDF/XML ontology file
     * @param entityMappings   entity mappings output file
     * @param relationMappings relation mappings output file
     * @param tripleMappings   triple mappings output file
     * @throws IOException
     */
    public static void export(File RDFontologyFile, File entityMappings, File relationMappings, File tripleMappings) throws IOException {
        Model model = ModelFactory.createDefaultModel();
        InputStream in = new FileInputStream(RDFontologyFile);
        model.read(in, "RDF/XML");

        Map<String, Long> entity2id = new HashMap<>();
        Map<String, Long> relation2id = new HashMap<>();

        CSVPrinter entityMappingsPrinter = null;
        CSVPrinter relationMappingsPrinter = null;
        CSVPrinter tripleMappingsPrinter = null;

        try {
            entityMappingsPrinter = new CSVPrinter(new FileWriter(entityMappings), CSVFormat.TDF);
            relationMappingsPrinter = new CSVPrinter(new FileWriter(relationMappings), CSVFormat.TDF);
            tripleMappingsPrinter = new CSVPrinter(new FileWriter(tripleMappings), CSVFormat.TDF);

            final StmtIterator stmtIterator = model.listStatements();

            long entityCounter = 0;
            long relationCounter = 0;

            while (stmtIterator.hasNext()) {
                Statement triple = stmtIterator.next();

                String subject = triple.getSubject().toString();
                String predicate = triple.getPredicate().toString();
                String object = triple.getObject().toString();

                if (!entity2id.containsKey(subject)) {
                    entity2id.put(subject, entityCounter);
                    entityMappingsPrinter.printRecord(subject, entityCounter);
                    entityCounter++;
                }
                Long idSubject = entity2id.get(subject);

                if (!relation2id.containsKey(predicate)) {
                    relation2id.put(predicate, relationCounter);
                    relationMappingsPrinter.printRecord(predicate, relationCounter);
                    relationCounter++;
                }
                Long idRelation = relation2id.get(predicate);

                if (!entity2id.containsKey(object)) {
                    entity2id.put(object, entityCounter);
                    entityMappingsPrinter.printRecord(object, entityCounter);
                    entityCounter++;
                }
                Long idObject = entity2id.get(object);

                tripleMappingsPrinter.printRecord(idSubject, idRelation, idObject);
            }
        } finally {
            try {
                if (entityMappingsPrinter != null)
                    entityMappingsPrinter.close();
                if (relationMappingsPrinter != null)
                    relationMappingsPrinter.close();
                if (tripleMappingsPrinter != null)
                    tripleMappingsPrinter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String... args) {
        File ontologyFile = new File(args[0]);
        File entityMappingFile = new File(args[1]);
        File relationMappingFile = new File(args[2]);
        File tripleMappingFile = new File(args[3]);

        try {
            TripleExporter.export(ontologyFile, entityMappingFile, relationMappingFile, tripleMappingFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
