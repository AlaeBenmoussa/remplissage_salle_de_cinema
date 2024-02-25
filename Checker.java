import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Checker {
    private Salle salle;
    private Remplissage remplissage;

    public Checker(String salleData, String contraintesData, String reservationsData, String remplissageData) {
        salle = new Salle();
        salle.readSalleData(salleData);
        salle.readConstraintData(contraintesData);
        salle.readReservationData(reservationsData);
        remplissage = readRemplissageData(remplissageData);
    }

    private float parseOccupancyRate(String rateStr) {
        String[] parts = rateStr.split("/");
        if (parts.length == 2) {
            float numerator = Float.parseFloat(parts[0]);
            float denominator = Float.parseFloat(parts[1]);
            return numerator / denominator;
        } else {
            throw new IllegalArgumentException("Invalid occupancy rate format: " + rateStr);
        }
    }

    private Remplissage readRemplissageData(String fileName) {
        Remplissage remplissage = new Remplissage(salle);
        try {
            File fichier = new File(fileName);
            Scanner sc = new Scanner(fichier);

            // Read header
            int nbRangeeUtilise = sc.nextInt();
            int sommeDist = sc.nextInt();
            String occupancyRateStr = sc.next();
            float tauxRemplissage = parseOccupancyRate(occupancyRateStr);
            sc.nextLine(); // Skip the rest of the line

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.equals("Non places")) {
                    break; // End of the seating data
                }
                Scanner lineScanner = new Scanner(line);
                int numGroupe = lineScanner.nextInt();
                int numRangee = lineScanner.nextInt();
                List<Integer> numGroupeSpectateur = new ArrayList<>();
                while (lineScanner.hasNextInt()) {
                    numGroupeSpectateur.add(lineScanner.nextInt());
                }
                int nbPlaceUtilisee = numGroupeSpectateur.remove(numGroupeSpectateur.size() - 1);
                remplissage.data.add(new RemplissageGroupeRangee(numGroupe, numRangee, nbPlaceUtilisee, numGroupeSpectateur));
                lineScanner.close();
            }

            sc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return remplissage;
    }

    public boolean check() {
        return checkConstraints() && checkPerformanceCriteria();
    }

    private boolean checkConstraints() {
        // Check if groups in the same row are at least Q seats apart
        for (RemplissageGroupeRangee rgr : remplissage.data) {
            int lastSeat = -salle.Q; // Start before the first seat
            for (int groupIndex : rgr.numGroupeSpectateur) {
                Reservation res = salle.reservations.get(groupIndex - 1);
                if (lastSeat + salle.Q > res.nombreSpectateur) {
                    System.out.println("Contrainte Q non respectée : les groupes sont trop proches dans la rangée " + rgr.numRangee);
                    return false; // Groups are too close to each other
                }
                lastSeat = res.nombreSpectateur;
            }
        }

        // Check if there are at least P empty rows between occupied rows
        int lastOccupiedRow = -salle.P - 1; // Start before the first row
        for (RemplissageGroupeRangee rgr : remplissage.data) {
            if (rgr.numRangee <= lastOccupiedRow + salle.P) {
                System.out.println("Contrainte P non respectée : les rangées sont trop proches les unes des autres");
                return false; // Rows are too close to each other
            }
            lastOccupiedRow = rgr.numRangee;
        }

        // Check if each group of spectators is placed on a single row and not divided
        for (Reservation res : salle.reservations) {
            boolean found = false;
            for (RemplissageGroupeRangee rgr : remplissage.data) {
                if (rgr.numGroupeSpectateur.contains(res.numGroupeSpectateur)) {
                    if (found) {
                        System.out.println("Contrainte de groupe non respectée : le groupe " + res.numGroupeSpectateur + " est divisé entre les rangées");
                        return false; // Group is divided between rows
                    }
                    found = true;
                }
            }
            if (!found) {
                System.out.println("Contrainte de groupe non respectée : le groupe " + res.numGroupeSpectateur + " n'est pas placé");
                return false; // Group is not placed at all
            }
        }

        return true; // All constraints are satisfied
    }

    private boolean checkPerformanceCriteria() {
        // Check if the sum of the distances to the stage is correct
        int computedSumDist = 0;
        for (RemplissageGroupeRangee rgr : remplissage.data) {
            for (Rangees r : salle.rangees.get(rgr.numRangee)) {
                if (r.groupe == rgr.numGroupe) {
                    computedSumDist += r.distanceDeLaScene;
                }
            }
        }
        if (computedSumDist != remplissage.sommeDist) {
            System.out.println("Critère de performance non respecté : la somme des distances ne correspond pas");
            return false; // Sum of distances does not match
        }

        // Check if the filling rate is correctly calculated
        int totalSeats = salle.nb_place_tot;
        int usedSeats = remplissage.nb_util_tot;
        float computedFillingRate = (float) usedSeats / totalSeats;
        if (Math.abs(computedFillingRate - remplissage.tauxRemplissage) > 0.01) { // Allowing a small margin for floating point comparisons
            System.out.println("Critère de performance non respecté : le taux de remplissage ne correspond pas");
            return false; // Filling rate does not match
        }

        return true; // Performance criteria are correct
    }


    public static void main(String[] args) {
        // Example usage
        Checker checker = new Checker(
                "DATA_A/Salle06/Salle06.txt",
                "DATA_A/Salle06/Contraintes01..txt",
                "DATA_A/Salle06/Reservations01.txt",
                "Remplissage.res"
        );

        if (checker.check()) {
            System.out.println("La vérification a réussi, toutes les contraintes et critères de performance sont respectés.");
        } else {
            System.out.println("La vérification a échoué, certaines contraintes ou critères de performance ne sont pas respectés.");
        }
    }
}
