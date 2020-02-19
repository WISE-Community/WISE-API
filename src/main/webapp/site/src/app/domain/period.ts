import { Workgroup } from "./workgroup";

export class Period {
  id: number;
  name: string;
  workgroups: Workgroup[] = [];
}
