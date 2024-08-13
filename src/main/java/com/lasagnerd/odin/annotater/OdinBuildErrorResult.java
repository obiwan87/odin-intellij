package com.lasagnerd.odin.annotater;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Data
public class OdinBuildErrorResult {

    @SerializedName("error_count")
    private int errorCount;

    @SerializedName("errors")
    private List<ErrorDetails> errors;

    @Setter
    @Getter
    static
    class ErrorDetails {

        @SerializedName("type")
        private String type;

        @SerializedName("pos")
        private Position pos;

        @SerializedName("msgs")
        private List<String> msgs;

    }

    @Setter
    @Getter
    static
    class Position {

        @SerializedName("file")
        private String file;

        @SerializedName("offset")
        private int offset;

        @SerializedName("line")
        private int line;

        @SerializedName("column")
        private int column;

        @SerializedName("end_column")
        private int endColumn;

    }

}
