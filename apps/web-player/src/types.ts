export interface League {
  id: string;
  name: string;
  description?: string;
  format: string;
  startTime?: string;
  endTime?: string;
}

export interface Match {
  id: string;
  player1Id: string;
  player2Id: string;
  player1Name: string;
  player2Name: string;
  scheduledTime: string;
  checkedIn?: boolean;
  isCompleted?: boolean;
  scoreSubmitted?: boolean;
}
