package happiestminds.androidpublish.plugin

import com.google.api.services.androidpublisher.model.Track
import org.gradle.api.tasks.TaskAction

class PromoteToBetaTask extends PlayPublishTask {
    def log = project.logger

    @TaskAction
    promoteToBeta() {
        super.publish()
        println "Initializing.."

        List<Track> allTracks = edits.tracks().list(variant.applicationId, editId).execute().getTracks()

//        if(allTracks || allTracks.size() == 0){
//            log.warn("Could not find any tracks")
//            return
//        }

        Track alphaTrack = null

        for (int trIndex=0; trIndex < allTracks.size();trIndex++){

            if(allTracks.get(trIndex).getTrack().equals("alpha"))
                alphaTrack = allTracks.get(trIndex)
        }

//        if(alphaTrack) {
//            log.warn("Could not find any alpha track")
//            return
//        }

        List<Integer> alphaVersionCodes = alphaTrack.getVersionCodes()

//        if(alphaVersionCodes || alphaVersionCodes.size() == 0) {
//            log.warn("Could not find any apk in alpha track")
//            return
//        }

        println "Found apk in alpha track.."

        def versionCode = alphaVersionCodes.get(0)
        alphaTrack.setVersionCodes(new ArrayList<Integer>())//.Clear()
        edits.tracks().patch(variant.applicationId, editId, "alpha", alphaTrack).execute()

        //Updating a Beta track with the same version code as in the Alpha track
        List<Integer> apkVersionCodes = new ArrayList<>()
        apkVersionCodes.add(versionCode)

        Track betaTrack = new Track()
        betaTrack.setTrack("beta")
        betaTrack.setVersionCodes(apkVersionCodes)

        println "Promoting to beta track.."

        edits.tracks().update(variant.applicationId, editId, "beta", betaTrack).execute()

        edits.commit(variant.applicationId, editId).execute()
    }

}
