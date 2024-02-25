import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Remplissage {
    Salle salle;
    int nbRangeeUtilise, sommeDist;
    float tauxRemplissage;
    List<RemplissageGroupeRangee> data;
//  attribut ajouté car pratique n'est pas dans l'UML
    int nb_util_tot;


    public Remplissage(Salle salle){
        this.data = new ArrayList<RemplissageGroupeRangee>();
        this.salle = new Salle(salle);
        sommeDist = 0;
        nbRangeeUtilise = 0;
        nb_util_tot = 0;


        AlgoEnumPartielle(1);
        // this.AlgorithmeEnumeration();
        // this.algoNaive();
        
        tauxRemplissage = (float)nb_util_tot/(float)salle.nb_place_tot;
    }
    
    public Remplissage(Remplissage r){
        this.data = new ArrayList<RemplissageGroupeRangee>();
        for (RemplissageGroupeRangee rgr : r.data) {
            this.data.add(new RemplissageGroupeRangee(rgr));
        }
        this.salle = new Salle(r.salle);
        sommeDist = r.sommeDist;
        nbRangeeUtilise = r.nbRangeeUtilise;
        nb_util_tot = r.nb_util_tot;
        tauxRemplissage = r.tauxRemplissage;
    }

    //ici le parametre demarrage est la rangee a la quelle on commance a remplire;
    public void algoNaive(){
        int k = 1;
        // tant quil reste des reservation et des rangee de libre
        while(!salle.reservations.isEmpty() &&  k < salle.rangees.size()){
            RempliRangee(k);
            k += salle.P + 1;
        }
    }

    public boolean RestePlace(int reservation, int decal, int capacitee){
        return (reservation + decal*salle.Q <= capacitee);
    }

    public void AlgorithmeEnumeration(){
        ArrayList<ArrayList<Reservation>> permutation = new ArrayList<ArrayList<Reservation>>();
        generatePermutationsHelper(salle.reservations, 1, permutation);

        // on minimise le nombre de range ou les distance de la scene
        int min = 50000;
        // ou on maximise le taux de remplissage
        // float max = 0.0f ;
        Remplissage rtemp = null;
        for (ArrayList<Reservation> reserv : permutation) {
            Remplissage r = new Remplissage(this);
            r.salle.reservations = reserv;
            r.AlgorithmeEnumerationRec(1);;

            if (min > r.sommeDist) {
                rtemp = r;
                min = r.sommeDist;
            }
        }

        if(rtemp == null) return;
        this.salle = new Salle(rtemp.salle);
        this.nbRangeeUtilise = rtemp.nbRangeeUtilise;
        this.nb_util_tot = rtemp.nb_util_tot;
        this.sommeDist = rtemp.sommeDist;
        this.tauxRemplissage = rtemp.tauxRemplissage;
        this.data.clear();
        for (RemplissageGroupeRangee remplissageGroupeRangee : rtemp.data) {
            this.data.add(remplissageGroupeRangee);
        }
    }

    public void AlgorithmeEnumerationRec(int k){
        // on minimise le nombre de range ou les distance de la scene
        int min = 50000;
        // ou on maximise le taux de remplissage
        // float max = 0.0f ;
        Remplissage rtemp = null;
        //on essaye de commencer par des rangee differente et regarder le quelle est optimal;
        for (int i = k; i <= salle.P+k; i++) {
            if(i >= salle.rangees.size())
                break;
            Remplissage r = new Remplissage(this);
            r.RempliRangee(i);
            //on passe a la prochaine remplissable
            r.AlgorithmeEnumerationRec(i+ salle.P +1);
            if (min > r.sommeDist) {
                rtemp = r;
                min = r.sommeDist;
            }
        }

        // on prend la meilleur solution 
        if(rtemp == null) return;
        this.salle = new Salle(rtemp.salle);
        this.nbRangeeUtilise = rtemp.nbRangeeUtilise;
        this.nb_util_tot = rtemp.nb_util_tot;
        this.sommeDist = rtemp.sommeDist;
        this.tauxRemplissage = rtemp.tauxRemplissage;
        this.data.clear();
        for (RemplissageGroupeRangee remplissageGroupeRangee : rtemp.data) {
            this.data.add(remplissageGroupeRangee);
        }

    }

    public void AlgoEnumPartielle(int k){
        // on minimise le nombre de range ou les distance de la scene
        int min = 50000;
        // ou on maximise le taux de remplissage
        // float max = 0.0f ;
        Remplissage rtemp = null;
        /********************************************************** */
        List<Integer> tabDoublons = new ArrayList<Integer>();
        // tabDoublons.add(salle.reservations.get(0).nombreSpectateur);
        for (int j = 0; j < salle.reservations.size(); j++) {
            while (j < salle.reservations.size() && tabDoublons.contains(salle.reservations.get(j).nombreSpectateur)) {
                j++;
            }
            if(j == salle.reservations.size())
                break;
            /******permut******* */
            Reservation temp;
            temp = salle.reservations.get(j);
            salle.reservations.set(j, salle.reservations.get(0));
            salle.reservations.set(0, temp);
            /*************** */

            //on essaye de commencer par des rangee differente et regarder le quelle est optimal;
            for (int i = k; i <= salle.P+k; i++) {
                if(i >= salle.rangees.size())
                    break;
                Remplissage r = new Remplissage(this);
                r.RempliRangee(i);
                //on passe a la prochaine remplissable
                // System.out.println("min : "+min + " brone : " + r.BorneInf(i+r.salle.P+1));
                if (min > r.BorneInfSommeDist(i+r.salle.P+1)) {
                    r.AlgoEnumPartielle(i+ salle.P +1);
                    if (min > r.sommeDist) {
                        rtemp = r;
                        min = r.sommeDist;
                    }
                }
                else
                    break;
            }

            /******on remet dans la meme config que au debut******* */
            temp = salle.reservations.get(j);
            salle.reservations.set(j, salle.reservations.get(0));
            salle.reservations.set(0, temp);
            /*************** */

        }
        /********************************************************** */

        // on prend la meilleur solution 
        if(rtemp == null) return;
        this.salle = new Salle(rtemp.salle);
        this.nbRangeeUtilise = rtemp.nbRangeeUtilise;
        this.nb_util_tot = rtemp.nb_util_tot;
        this.sommeDist = rtemp.sommeDist;
        this.tauxRemplissage = rtemp.tauxRemplissage;
        this.data.clear();
        for (RemplissageGroupeRangee remplissageGroupeRangee : rtemp.data) {
            this.data.add(remplissageGroupeRangee);
        }

    }


    /*Algo qui calcule la borne inferieur*/

    public int BorneInfNbRange(int i){
        int res = 0;
        int resTot = 0;
        for (Reservation nb : salle.reservations) {
            resTot += nb.nombreSpectateur;
        }
        while (resTot > 0 && i < salle.rangees.size()) {
            for (Rangees r : salle.rangees.get(i)) {
                resTot -= r.capacitee;
                res++;
                if (resTot <= 0) {
                    return res;
                }
            }
            i += salle.P+1;
        }
        return res + nbRangeeUtilise;
    }

    public int BorneInfSommeDist(int i){
        int res = 0;
        int resTot = 0;
        for (Reservation nb : salle.reservations) {
            resTot += nb.nombreSpectateur;
        }
        while (resTot > 0 && i < salle.rangees.size()) {
            for (Rangees r : salle.rangees.get(i)) {
                resTot -= r.capacitee+salle.P;
                res += i;
                if (resTot <= 0) {
                    return res;
                }
            }
            i += salle.P+1;
        }
        return res + sommeDist;
    }

    /*********************************/

    private void RempliRangee(int i){
        int decal = 0;
        int nbutil = 0;
        List<Integer> gs = new ArrayList<Integer>();
        for (Rangees r : salle.rangees.get(i)) {
            if(salle.reservations.isEmpty())
                break;
            while(RestePlace(salle.reservations.get(0).nombreSpectateur, decal, r.capacitee)){
                r.capacitee -= salle.reservations.get(0).nombreSpectateur + decal*salle.Q;
                nbutil += salle.reservations.get(0).nombreSpectateur;
                gs.add(salle.reservations.get(0).numGroupeSpectateur);
                salle.reservations.remove(0);
                decal = 1;
                if(salle.reservations.isEmpty())
                    break;
            }
            decal = 0;
            sommeDist += i;
            data.add(new RemplissageGroupeRangee(r.groupe, i, nbutil, gs));
            gs.clear();
            nb_util_tot += nbutil;
            nbutil = 0;
            decal = 0;
            nbRangeeUtilise++;
        }

        tauxRemplissage = (float)nb_util_tot / (float)salle.nb_place_tot;
    }

    //on genere les permutation des reservation
    public void generatePermutationsHelper(List<Reservation> reservations, int index, ArrayList<ArrayList<Reservation>> permutations) {
        if (index == reservations.size() - 1) {
            permutations.add(new ArrayList<>(reservations));
        } else {
            Set<Integer> set = new HashSet<>();
            for (int i = index; i < reservations.size(); i++) {
                if (!set.contains(reservations.get(i).nombreSpectateur)) {
                    set.add(reservations.get(i).nombreSpectateur);

                    // Échanger les réservations
                    Reservation temp = reservations.get(index);
                    reservations.set(index, reservations.get(i));
                    reservations.set(i, temp);

                    // Appeler la fonction récursivement pour la prochaine position
                    generatePermutationsHelper(reservations, index + 1, permutations);

                    // Revertir l'échange pour explorer d'autres permutations
                    temp = reservations.get(index);
                    reservations.set(index, reservations.get(i));
                    reservations.set(i, temp);
                }
            }
        }
    }

    public void AffichePermut(ArrayList<ArrayList<Reservation>> permutations){
        for (ArrayList<Reservation> listeReserv : permutations) {
            for (Reservation reserv : listeReserv) {
                System.out.print(reserv.nombreSpectateur + " ");
            }
            System.out.println();
        }
    }

    public String ToString(){
        String res = "";
        res += nbRangeeUtilise + " " + sommeDist + " " + (int)nb_util_tot +"/"+ (int)salle.nb_place_tot + "\n";
        for (RemplissageGroupeRangee remplissageGroupeRangee : data) {
            res += remplissageGroupeRangee.numGroupe + " " + remplissageGroupeRangee.numRangee;
            for (Integer gs : remplissageGroupeRangee.numGroupeSpectateur) {
                res += " " + gs;
            }
            res += " " + remplissageGroupeRangee.nbPlaceUtilisee + "\n";
        }
        res += "Non places\n";
        if (salle.reservations.isEmpty()) {
            res += "-1\n";
        }
        for (Reservation r : salle.reservations) {
            res += r.numGroupeSpectateur + " ";
        }
        res += "\n";
        return res;
    }
}
