package org.fiddich.api.domain.timetable.helper;

import org.fiddich.api.domain.everytime.dto.Subject;
import org.fiddich.api.domain.everytime.dto.TimePlace;

import java.util.*;
import java.util.stream.Collectors;

public class TimetableGenerator {

    private final List<Subject> totalSubjects;

    private int targetMajorCnt;
    private int targetCultureCnt;
    private List<Subject> likeSubjects;
    private List<Subject> dislikeSubjects;

    private int[][] usedTime = new int[7][1440];
    private List<List<Subject>> makedTimeTable;

    public TimetableGenerator(List<Subject> totalSubjects) {
        this.totalSubjects = totalSubjects;
    }

    public void create(int targetMajorCnt, int targetCultureCnt, List<Subject> likeSubjects, List<Subject> dislikeSubjects, int includedMajorCnt, int includedCultureCnt, int[][] usedTime) {
        this.targetMajorCnt = targetMajorCnt;
        this.targetCultureCnt = targetCultureCnt;
        this.likeSubjects = likeSubjects;
        this.dislikeSubjects = dislikeSubjects;
        this.makedTimeTable = new ArrayList<>();
        this.usedTime = usedTime;

        List<Subject> v = new ArrayList<>();
        // 포함될 강의 개수 추가
        SubjectCnt subjectCnt = new SubjectCnt(includedMajorCnt, includedCultureCnt);

        go(-1, v, subjectCnt);

        System.out.println(makedTimeTable.size());
    }



    void go(int start, List<Subject> b, SubjectCnt nowSubjectCnt) {

        if(nowSubjectCnt.major == targetMajorCnt && nowSubjectCnt.culture == targetCultureCnt) {
            makedTimeTable.add(new ArrayList<>(b)); // ← 복사해서 추가
            return;
        }

        for(int i = start + 1; i < totalSubjects.size(); i++) {

            // ❌ dislikeLecture에 포함된 강의는 건너뜀
            if (dislikeSubjects.contains(totalSubjects.get(i))) {
                continue;
            }

            if(canAddLectureAboutCode(b, totalSubjects.get(i)) && canAddLectureAboutTime(totalSubjects.get(i)) && canAddLectureAboutCnt(nowSubjectCnt, totalSubjects.get(i))) {
                b.add(totalSubjects.get(i));
                fillUsedTime(totalSubjects.get(i));
                addLectureCnt(totalSubjects.get(i), nowSubjectCnt);

                go(i, b, nowSubjectCnt);

                b.remove(b.size() - 1);
                eraseUsedTime(totalSubjects.get(i));
                removeLectureCnt(totalSubjects.get(i), nowSubjectCnt);
            }
        }
    }

    // 겹치는 학수번호 제외
    boolean canAddLectureAboutCode(List<Subject> selectedSubjects, Subject targetSubject) {
        String targetCode = targetSubject.getSubjectCode();
        // 1. 이미 같은 코드가 존재하는지 확인
        boolean isDuplicateCode = selectedSubjects.stream()
                .map(Subject::getSubjectCode)
                .collect(Collectors.toSet())
                .contains(targetCode);

        if (isDuplicateCode) return false;
        return true;
    }


    // 겹치는 시간 제외
    boolean canAddLectureAboutTime(Subject targetSubject) {
        // 2. 시간 겹침 확인
        List<TimePlace> timeplaceList = targetSubject.getTimeplaceList();
        for (TimePlace timePlace : timeplaceList) {
            if(timePlace == null) {
                continue;
            }
            int day = Integer.parseInt(timePlace.getDay());
            int start = Integer.parseInt(timePlace.getStart()) * 5;
            int end = Integer.parseInt(timePlace.getEnd()) * 5;

            for (int i = start; i < end; i++) {
                if (usedTime[day][i] == 1) return false; // 이미 사용 중인 시간
            }
        }

        return true;
    }

    // 강의 개수 조건 제외
    boolean canAddLectureAboutCnt(SubjectCnt nowSubjectCnt, Subject targetSubject) {
        if(isCultureSubject(targetSubject)) {
            return nowSubjectCnt.culture + 1 <= targetCultureCnt;
        }
        else {
            return nowSubjectCnt.major + 1 <= targetMajorCnt;
        }
    }

    // 시간 칠하기
    void fillUsedTime(Subject targetSubject) {
        List<TimePlace> timePlaceList = targetSubject.getTimeplaceList();
        for (TimePlace timePlace : timePlaceList) {
            if(timePlace == null) {
                continue;
            }
            int day = Integer.parseInt(timePlace.getDay());
            int start = Integer.parseInt(timePlace.getStart()) * 5;
            int end = Integer.parseInt(timePlace.getEnd()) * 5;

            for(int i = start; i < end; i++) {
                usedTime[day][i] = 1;
            }
        }
    }

    // 시간 지우기
    void eraseUsedTime(Subject targetSubject) {
        List<TimePlace> timePlaceList = targetSubject.getTimeplaceList();
        for (TimePlace timePlace : timePlaceList) {
            if(timePlace == null) {
                continue;
            }
            int day = Integer.parseInt(timePlace.getDay());
            int start = Integer.parseInt(timePlace.getStart()) * 5;
            int end = Integer.parseInt(timePlace.getEnd()) * 5;

            for(int i = start; i < end; i++) {
                usedTime[day][i] = 0;
            }
        }
    }

    // 강의 개수 추가
    void addLectureCnt(Subject targetSubject, SubjectCnt p) {
        if(isCultureSubject(targetSubject)) {
            p.culture++;
        }
        else {
            p.major++;
        }
    }
    // 강의 개수 제거
    void removeLectureCnt(Subject targetSubject, SubjectCnt p) {
        if(isCultureSubject(targetSubject)) {
            p.culture--;
        }
        else {
            p.major--;
        }
    }



    private boolean isCultureSubject(Subject subject) {
        if(subject.getType().equals("교양")) {
            return true;
        }
        return false;
    }

    static class SubjectCnt {
        int major;
        int culture;

        public SubjectCnt(int major, int culture) {
            this.major = major;
            this.culture = culture;
        }
    }


    public List<List<Subject>> getMakedTimeTable(int minCredit, int maxCredit) {
        return makedTimeTable.stream().filter(t -> {
            int credit = getTotalCredit(t);
            return credit >= minCredit && credit <= maxCredit;
        }).toList();
    }


    // 학점 계산
    int getTotalCredit(List<Subject> subjects) {
        int sum = 0;
        for(Subject subject : subjects) {
            sum += Integer.parseInt(subject.getCredit());
        }
        return sum;
    }

}
