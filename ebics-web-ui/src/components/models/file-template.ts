export interface FileTemplate {
    id?: number,
    fileContentText: string,
    templateName: string,
    templateTags: string,
    //False => out of the box template (source code maintained)
    //True => custom template from DB
    custom: boolean,
    creatorUserId: string,
    shared: boolean,
}