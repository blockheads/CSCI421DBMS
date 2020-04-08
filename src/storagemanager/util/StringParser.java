package storagemanager.util;

public class StringParser {

    public static String toLowerCaseNonString(String string){
        StringBuilder newString = new StringBuilder();
        boolean inQuote = false;
        for(char letter: string.toCharArray()){
            if(letter == '\"')
                inQuote = !inQuote;

            if(!inQuote){
                letter = Character.toLowerCase(letter);
            }

            newString.append(letter);
        }

        return newString.toString();
    }


}
