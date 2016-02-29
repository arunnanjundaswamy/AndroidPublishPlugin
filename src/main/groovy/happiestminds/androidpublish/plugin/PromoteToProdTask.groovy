package happiestminds.androidpublish.plugin

import com.google.api.services.androidpublisher.model.Track
import org.gradle.api.tasks.TaskAction

class PromoteToProdTask extends PlayPublishTask {
    def log = project.logger

    @TaskAction
    promoteToProd() {
        super.publish()
        println "Initializing.."

        List<Track> allTracks = edits.tracks().list(variant.applicationId, editId).execute().getTracks()

//        if(allTracks.equals(null) || allTracks.size() == 0){
//            log.warn("Could not find any tracks")
//            return
//        }

        Track betaTrack = null

        for (int trIndex=0; trIndex < allTracks.size();trIndex++){

            if(allTracks.get(trIndex).getTrack().equals("beta"))
                betaTrack = allTracks.get(trIndex)
        }

//        if(betaTrack) {
//            log.warn("Could not find any beta track")
//            return
//        }

        List<Integer> betaVersionCodes = betaTrack.getVersionCodes()

//        if(betaVersionCodes || betaVersionCodes.size() == 0) {
//            log.warn("Could not find any apk in beta track")
//            return
//        }

        println "Found apk in beta track.."

        def versionCode = betaVersionCodes.get(0)
        betaTrack.setVersionCodes(new ArrayList<Integer>())//.Clear()
        edits.tracks().patch(variant.applicationId, editId, "alpha", betaTrack).execute()

        //Updating a Production track with the same version code as in the Alpha track
        List<Integer> apkVersionCodes = new ArrayList<>()
        apkVersionCodes.add(versionCode)

        Track prodTrack = new Track()
        prodTrack.setTrack("production")

        if (extension.track?.equals("rollout")) {
            prodTrack.setUserFraction(extension.userFraction)
        }

        prodTrack.setVersionCodes(apkVersionCodes)

        println "Promoting to prod track.."

        edits.tracks().update(variant.applicationId, editId, "production", prodTrack).execute()

        edits.commit(variant.applicationId, editId).execute()
    }

}
