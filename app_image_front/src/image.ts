export interface ImageType {
  id: number;
  name: string;
  type: string;
  size: string;
  distance: number;
  tags: Array<string>;
  favorite: boolean;
}