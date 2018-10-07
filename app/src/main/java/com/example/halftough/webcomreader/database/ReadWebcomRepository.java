package com.example.halftough.webcomreader.database;

import android.os.AsyncTask;

public class ReadWebcomRepository {

    public static void updateChapterCount(String wid, int count, ReadWebcomsDAO dao){
        ReadWebcom webcom = new ReadWebcom(wid);
        webcom.setChapterCount(count);
        new updateChapterCountAsyncTask(dao).execute(webcom);
    }

    private static class updateChapterCountAsyncTask extends AsyncTask<ReadWebcom, Void, Void> {
        private ReadWebcomsDAO dao;
        updateChapterCountAsyncTask(ReadWebcomsDAO readWebcomsDAO){ dao = readWebcomsDAO; }
        @Override
        protected Void doInBackground(ReadWebcom... readWebcoms) {
            dao.updateChapterCount(readWebcoms[0].getWid(), readWebcoms[0].getChapterCount());
            return null;
        }
    }

    public static void setLastUpdateDate(String wid, String date, ReadWebcomsDAO dao){
        ReadWebcom webcom = new ReadWebcom(wid);
        webcom.setLastUpdated(date);
        new setLastUpdateDateAsyncTask(dao).execute(webcom);
    }

    private static class setLastUpdateDateAsyncTask extends AsyncTask<ReadWebcom, Void, Void>{
        private ReadWebcomsDAO dao;
        setLastUpdateDateAsyncTask(ReadWebcomsDAO readWebcomsDAO){ dao = readWebcomsDAO; }
        @Override
        protected Void doInBackground(ReadWebcom... readWebcoms) {
            dao.setLastUpdateDate(readWebcoms[0].getWid(), readWebcoms[0].getLastUpdated());
            return null;
        }
    }
}
