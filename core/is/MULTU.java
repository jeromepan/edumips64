/*
 * MULTU.java
 *
 * 18th may 2007
 * Instruction MULTU of the MIPS64 Instruction Set
 * (c) 2006 EduMips64 project - Giorgio Scibilia - Erik Urzi'- Sciuto Lorenzo
 *
 * This file is part of the EduMIPS64 project, and is released under the GNU
 * General Public License.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */


package edumips64.core.is;
import edumips64.core.*;
import edumips64.utils.*;

//per diagnostica
import java.util.*;

/**
 * <pre>
 *      Syntax: MULTU rs, rt
 * Description: (LO,HI) = rs * rt
 *              To multiply 32-bit unsigned integers
 *              The 32-bit word value in GPR rt is multiplied by the 32-bit 
 *              value in GPR rs, treating both operands as unsigned values.
 *		The low-order 32-bit word of the result is sign-extended and placed into special register
 *		LO, and the high-order 32-bit word is sign-extended and placed into special register HI.
 * </pre>
 * 
 * @author Giorgio Scibilia - Lorenzo Sciuto - Erik Urzi'
 */
class MULTU extends ALU_RType
{
	final int RS_FIELD=0;
	final int RT_FIELD=1;
	final String OPCODE_VALUE="011001";

	String lo;
	String hi;

	public MULTU()
	{
		super.OPCODE_VALUE = OPCODE_VALUE;
		syntax="%R,%R";
		name="MULTU";
	}
	public void ID() throws RAWException, IrregularWriteOperationException, IrregularStringOfBitsException {
	        //if source registers are valid passing their own values into temporary registers
	        Register rs=cpu.getRegister(params.get(RS_FIELD));
	        Register rt=cpu.getRegister(params.get(RT_FIELD));
	        if(rs.getWriteSemaphore()>0 || rt.getWriteSemaphore()>0)
	            throw new RAWException();
	        TR[RS_FIELD]=rs;
	        TR[RT_FIELD]=rt;
	        //locking the destination register 
	  
	        cpu.getLO().incrWriteSemaphore();
	        cpu.getHI().incrWriteSemaphore();
	}

	public void EX() throws IrregularStringOfBitsException,IntegerOverflowException,TwosComplementSumException 
	{
		//getting registers' values and cutting the first 32-bits
		String str_rs=TR[RS_FIELD].getBinString();
	    String str_rt=TR[RT_FIELD].getBinString();
		//cutting the high part of registers
		str_rs=str_rs.substring(32,64);
		str_rt=str_rt.substring(32,64);
		/*for (int i=str_rs.length();i<64;i++){
			str_rs='0'+str_rs;		
		}
		for (int i=str_rt.length();i<64;i++){
			str_rt='0'+str_rt;		
		}*/
		
		long rs = Converter.binToLong(str_rs,true);
		long rt = Converter.binToLong(str_rt,true);						
		long result = rs * rt;
		//converting result to a String of 64-bits
		String tmp = Long.toString(result,2);
		if(tmp.charAt(0)=='-')
			tmp=tmp.substring(1);
		for (int i=tmp.length();i<64;i++){
			tmp='0'+tmp;		
		}
		hi =tmp.substring(0,32);
		lo =tmp.substring(32);
		//performing sign extension
		for(int i=hi.length(); i<64; i++)
			hi = '0'+hi;
		for(int i=lo.length(); i<64; i++)
			lo = '0'+lo;

		if(enableForwarding)
		{
			doWB();
		}    
	}


	public void WB() throws IrregularStringOfBitsException 
	{
		if(!enableForwarding)
		{
			doWB();
		}
	}
	public void doWB() throws IrregularStringOfBitsException 
	{
	        //passing results from temporary registers to destination registers and unlocking them
	        Register lo = cpu.getLO();
		Register hi = cpu.getHI();
	        lo.setBits(this.lo,0);
		hi.setBits(this.hi,0);

	        lo.decrWriteSemaphore();
	        hi.decrWriteSemaphore();
	}
	public void pack() throws IrregularStringOfBitsException 
	{
	        //conversion of instruction parameters of "params" list to the "repr" form (32 binary value) 
	        repr.setBits(OPCODE_VALUE,OPCODE_VALUE_INIT);
	        repr.setBits(Converter.intToBin(RS_FIELD_LENGTH,params.get(RS_FIELD)),RS_FIELD_INIT);
	        repr.setBits(Converter.intToBin(RT_FIELD_LENGTH,params.get(RT_FIELD)),RT_FIELD_INIT);
	}   
   
    
}
