import React, { useEffect, useState } from 'react';
import { authFetch } from './api';

type Lesson = { id: number, proId: number, studentId: number, status: string, startsAt: string, endsAt: string };
type Availability = { id: number, instructorId: number, startAt: string, endAt: string, type: string };

export default function InstructorScheduleManager() {
  const [lessons, setLessons] = useState<Lesson[]>([]);
  const [availability, setAvailability] = useState<Availability[]>([]);
  const [loading, setLoading] = useState(false);
  useEffect(() => { fetchLessons(); fetchAvailability(); }, []);
  async function fetchLessons() {
    setLoading(true);
    const res = await authFetch('/api/lessons');
    if(res.ok) setLessons(await res.json());
    setLoading(false);
  }
  async function fetchAvailability() {
    setLoading(true);
    const res = await authFetch('/api/instructors/1/availability');
    if(res.ok) setAvailability(await res.json());
    setLoading(false);
  }
  return (
    <div className="mt-4 p-4 border rounded bg-gray-50">
      <h3 className="font-bold mb-2">Lessons / Book-a-Pro / Instructor Schedules</h3>
      <div>
        <h4 className="font-semibold">Availability</h4>
        <ul>{availability.map(a => <li key={a.id}>{a.startAt} - {a.endAt} ({a.type})</li>)}</ul>
      </div>
      <div className="mt-2">
        <h4 className="font-semibold">Lessons</h4>
        <ul>{lessons.map(l => <li key={l.id}>{l.startsAt} - {l.endsAt} | Pro: {l.proId} | Student: {l.studentId} | Status: {l.status}</li>)}</ul>
      </div>
    </div>
  );
}

