package com.lina.programme_biblio_tp4.service;

import com.lina.programme_biblio_tp4.dtos.emprunt.EmpruntDtoDocument;
import com.lina.programme_biblio_tp4.modele.Amende;
import com.lina.programme_biblio_tp4.modele.Client;
import com.lina.programme_biblio_tp4.modele.Document;
import com.lina.programme_biblio_tp4.modele.EmpruntDocuments;
import com.lina.programme_biblio_tp4.repository.AmendeRepository;
import com.lina.programme_biblio_tp4.repository.EmpruntDocumentRepository;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class ServiceEmpruntDocuments {

    private final double amende = 0.25;
    private EmpruntDocumentRepository empruntDocumentRepository;
    private AmendeRepository amendeRepository;

    public ServiceEmpruntDocuments(EmpruntDocumentRepository empruntDocumentRepository,
                                   AmendeRepository amendeRepository) {
        this.empruntDocumentRepository = empruntDocumentRepository;
        this.amendeRepository = amendeRepository;
    }

    public EmpruntDtoDocument saveEmpruntDocuments(Date dateInitial,
                                                 Date dateExpire,
                                                 int nbrRappel,
                                                 Client client,
                                                 Document document) {
        EmpruntDocuments empruntDocuments = empruntDocumentRepository.save(new EmpruntDocuments(
                dateInitial.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                dateExpire.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                nbrRappel,
                client,
                document));
        return empruntDocuments.toEmpruntDtoDocument();
    }

    public EmpruntDtoDocument saveEmpruntDocuments(EmpruntDocuments empruntDocuments) {
        return empruntDocumentRepository.save(empruntDocuments).toEmpruntDtoDocument();
    }

    public void removeEmpruntDocuments(EmpruntDocuments empruntDocuments) {
        empruntDocumentRepository.delete(empruntDocuments);
    }

    public void deleteAllEmpruntDocuments() {
        empruntDocumentRepository.deleteAll();
    }

    public EmpruntDtoDocument getEmpruntDocuments(long empruntDocumentsId) {
        return empruntDocumentRepository.findById(empruntDocumentsId)
                .orElseThrow(RuntimeException::new).toEmpruntDtoDocument();
    }

    public List<EmpruntDtoDocument> findAllEmpruntDocuments() {
        List<EmpruntDocuments> empruntDocuments = empruntDocumentRepository.findAll();
        List<EmpruntDtoDocument> empruntDtoDocuments = new ArrayList<>();

        for (EmpruntDocuments empruntDocument : empruntDocuments) {
            empruntDtoDocuments.add(empruntDocument.toEmpruntDtoDocument());
        }
        return empruntDtoDocuments;
    }

    public List<EmpruntDtoDocument> getClientEmprunt(long clientId) {
        List<EmpruntDocuments> clientEmprunts = empruntDocumentRepository.getClientEmprunt(clientId);
        List<EmpruntDtoDocument> clientEmpruntsDto = new ArrayList<>();

        for (EmpruntDocuments clientEmprunt : clientEmprunts) {
            clientEmpruntsDto.add(clientEmprunt.toEmpruntDtoDocument());
        }
        return clientEmpruntsDto;
    }

    public List<EmpruntDtoDocument> getAllClientsEmprunts() {
        List<EmpruntDocuments> empruntDocuments = empruntDocumentRepository.getAllClientsEmprunts();
        List<EmpruntDtoDocument> empruntDtoDocuments = new ArrayList<>();

        for (EmpruntDocuments empruntDocument : empruntDocuments) {
            empruntDtoDocuments.add(empruntDocument.toEmpruntDtoDocument());
        }
        return empruntDtoDocuments;
    }

    public int getNbrEmpruntExemplaire(long empruntDocumentsId) {
        return empruntDocumentRepository.getNbrEmpruntExemplaire(empruntDocumentsId);
    }

    public EmpruntDocuments getEmpruntsDocuments(long clientId, long documentId) {
        return empruntDocumentRepository.getEmpruntDocuments(clientId, documentId);
    }

    public double calculAmende(Calendar today, LocalDate dateExpire) {
        long diffInMillies = Math.abs(today.getTime().getTime() - dateExpire.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
        long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        return amende * diff;
    }

    public double getTotalAmendes(Client client, Calendar today) {
        double totalAmendes = 0;

        List<EmpruntDocuments> empruntDocuments = empruntDocumentRepository.getClientEmpruntRetard(client.getId());
        if (empruntDocuments.size() > 0) {
            for(int i = 0; i < empruntDocuments.size(); i++) {
                EmpruntDocuments empruntDocument = empruntDocuments.get(i);
                totalAmendes += calculAmende(today, empruntDocument.getDateExpire());
            }
        }

        List<Amende> amendes = amendeRepository.getClientAmendes(client.getId());
        if (amendes.size() > 0) {
            for(int i = 0; i < amendes.size(); i++) {
                Amende amende = amendes.get(i);
                totalAmendes += amende.getSommeAmende();
            }
        }

        return totalAmendes;
    }

    public String faireEmprunt(Client client, Document document) {
        String message = "";
        boolean peutEmprunter = true;

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        double totalFrais = getTotalAmendes(client, today);

        if (totalFrais > 0) {
            message = "\nEmprunt interdit pour cause des amendes " + totalFrais + "$ \n";
            peutEmprunter = false;
        }

        if (peutEmprunter) {
            int nbrEmprunt = getNbrEmpruntExemplaire(document.getId());
            if (nbrEmprunt >= document.getNbrExemplaire()) {
                message = "Tous les exemplaires ont été emprunté";
                peutEmprunter = false;
            }
        }

        if (peutEmprunter) {
            double tarifEmprunt;
            int periodeEmprunt;
            if (client.getVille().equalsIgnoreCase("JavaTown")) {
                //les résidents de Javatown, peuvent emprunter gratuitement
                tarifEmprunt = 0;
            } else {
                tarifEmprunt = 1;
            }
            if (document.getGenreDocument().equalsIgnoreCase(Document.C_LIVRE)) {
                periodeEmprunt = 3;
            } else if (document.getGenreDocument().equalsIgnoreCase(Document.C_CD)) {
                periodeEmprunt = 2;
            } else {
                //DVD
                periodeEmprunt = 1;
            }

            Calendar dateExpire = today;
            dateExpire.add(Calendar.WEEK_OF_YEAR, periodeEmprunt);

            var empruntDocument = new EmpruntDocuments(
                    today.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                    dateExpire.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                    0,
                    client,
                    document);
            saveEmpruntDocuments(empruntDocument);
            var e = getEmpruntDocuments(empruntDocument.getId());
            message = e.toString();
        }
        return message;
    }

    public BigInteger[] getNbrEmpruntParMois() {

        List<Object[]> empruntParMois = empruntDocumentRepository.getNbrEmpruntParMois();

        BigInteger[] nbrEmpruntParMois = new BigInteger[] {BigInteger.valueOf(0),
                BigInteger.valueOf(0),
                BigInteger.valueOf(0),
                BigInteger.valueOf(0),
                BigInteger.valueOf(0),
                BigInteger.valueOf(0),
                BigInteger.valueOf(0),
                BigInteger.valueOf(0),
                BigInteger.valueOf(0),
                BigInteger.valueOf(0),
                BigInteger.valueOf(0),
                BigInteger.valueOf(0)};
        for(int i = 0; i < empruntParMois.size(); i++) {
            Object[] empruntMois = empruntParMois.get(i);
            nbrEmpruntParMois[(int)empruntMois[0] - 1] = (BigInteger)empruntMois[1];
        }

        return nbrEmpruntParMois;
    }

    public String retourDocument(Client client, Document document, Date dateRetour) {
        String message = "";
        Calendar today = Calendar.getInstance();
        today.setTime(dateRetour);

        EmpruntDocuments empruntDocuments = getEmpruntsDocuments(client.getId(), document.getId());
        if (empruntDocuments.getDateExpire().isBefore(dateRetour.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())) {
            double sommeAmende = calculAmende(today, empruntDocuments.getDateExpire());

            Amende amende = new Amende(empruntDocuments.getDateInitial(),
                    empruntDocuments.getDateExpire(), empruntDocuments.getNbrRappel(), document,
                    client, sommeAmende);
            amendeRepository.save(amende);
            double totalFrais = getTotalAmendes(client, today);

            message = "\nFrais d'amendes " + sommeAmende + "$" +
                    "\nTotal des amendes " + totalFrais + "$";
        }

        empruntDocumentRepository.delete(empruntDocuments);

        return message;
    }
}