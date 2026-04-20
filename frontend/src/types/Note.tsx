export interface Note {
    id: number;
    title: string;    
    content: string;
    active: boolean;
    createdAt: string;
    updatedAt: string | null;
}
