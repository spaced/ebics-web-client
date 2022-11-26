import { FileFormat } from 'components/models/file-format';

export interface FileTemplate {
    id?: number,
    fileContentText: string,
    templateName: string,
    templateTags: string,
    fileFormat: FileFormat,
    //False => out of the box template (source code maintained)
    //True => custom template from DB
    custom: boolean,
    canBeEdited: boolean,
    creatorUserId: string,
    guestAccess: boolean,
}